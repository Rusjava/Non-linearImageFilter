/*
 * Copyright (C) 2015 Ruslan Feshchenko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package NonLinearImageFilter;

import CrankNicholson2D.CrankNicholson2D;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.awt.color.ColorSpace;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.image.ColorModel;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.Manifest;
import java.util.function.DoubleFunction;

import javax.imageio.ImageIO;
import TextUtilities.MyTextUtilities;
import java.awt.Transparency;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import javax.media.MediaLocator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JComponent;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Ruslan Feshchenko
 * @version 2.2
 */
public class NonLinearImageFilter extends javax.swing.JFrame {

    /**
     * Creates new form NonLinearImageFilter
     */
    private final ImageParam imageParam;
    private ArrayList<JComponent> imageList;
    private int nSteps = 10, threadNumber, sliderposition = 50;
    private double precision = 1e-10, diffCoef = 0.01, nonLinearCoef = 10000,
            anisotropy = 0, iterationCoefficient = 0.5;
    private boolean nonLinearFlag = false, working = false;
    private CrankNicholson2D comp;
    private final Map defaults;
    private SwingWorker<Void, Void> worker;
    private ArrayList<double[][]> dataList;
    private final JFormattedTextField xsizeField, ysizeField, noiseField, signalField,
            scaleField, precisionField, anisotropyField, frameRateField, threadNumberField, iterField;
    private final JComboBox bitNumberMenu;
    private final JComboBox<String> funcBox;
    private final ResourceBundle bundle;
    private final FileFilter[] filters;
    private int frameRate = 10, videoFormat = 0;
    private final int[] bitnesses = new int[]{8, 16, 32};
    private final double[] nonLinearCoefs = new double[]{30, 1e4, 1e8};
    private File imageRFile = null, imageWFile = null, videoWFile = null;
    private final DoubleFunction[] funcs;

    public NonLinearImageFilter() {
        this.threadNumber = Runtime.getRuntime().availableProcessors();
        this.imageList = new ArrayList<>();
        this.defaults = new HashMap();
        this.imageParam = new ImageParam();
        this.xsizeField = MyTextUtilities.getIntegerFormattedTextField(300, 2, 10000);
        this.ysizeField = MyTextUtilities.getIntegerFormattedTextField(200, 2, 10000);
        this.noiseField = MyTextUtilities.getIntegerFormattedTextField(imageParam.bitNumber - 2, 1, imageParam.bitNumber - 1);
        this.signalField = MyTextUtilities.getIntegerFormattedTextField(imageParam.bitNumber - 1, 1, imageParam.bitNumber);
        this.scaleField = MyTextUtilities.getDoubleFormattedTextField(0.5, 0.1, 1.0, false);
        this.precisionField = MyTextUtilities.getDoubleFormattedTextField(1e-06, 1e-10, 1e-2, true);
        this.anisotropyField = MyTextUtilities.getDoubleFormattedTextField(0.0, 0.0, 1.0, false);
        this.frameRateField = MyTextUtilities.getIntegerFormattedTextField(10, 1, 100);
        this.threadNumberField = MyTextUtilities.getIntegerFormattedTextField(threadNumber, 1, 10);
        this.iterField = MyTextUtilities.getDoubleFormattedTextField(0.3, 0.0, 1.0, false);
        this.bundle = ResourceBundle.getBundle("NonLinearImageFilter/Bundle");
        filters = new FileFilter[]{
            new FileNameExtensionFilter("tif/tiff", "tif", "tiff"),
            new FileNameExtensionFilter("png", "png"),
            new FileNameExtensionFilter("gif", "gif")
        };
        this.funcs = new DoubleFunction[]{p -> 1 / (1 + p), p -> Math.exp(-p)};
        funcBox = new JComboBox<>();
        funcBox.addItem(bundle.getString("FUNCTION 1"));
        funcBox.addItem(bundle.getString("FUNCTION 2"));

        this.bitNumberMenu = new JComboBox(new String[]{"8 bit", "16 bit", "32 bit"});
        bitNumberMenu.setSelectedIndex(1);
        bitNumberMenu.addItemListener((java.awt.event.ItemEvent evt) -> {
            jComboBoxBitNumberActionPerformed(evt);
        });

        UIManager.addPropertyChangeListener(e -> SwingUtilities.updateComponentTreeUI(this));
        initComponents();
        jButtonStart.setEnabled(false);
        ButtonGroup LFGroup = new ButtonGroup();
        LFGroup.add(jRadioButtonMenuItemDefault);
        LFGroup.add(jRadioButtonMenuItemSystem);
        LFGroup.add(jRadioButtonMenuItemNimbus);
        jLabelThreads.setText(bundle.getString("NonLinearImageFilter.jLabelThreads.text") + threadNumber);
        jLabelProcessors.setText(bundle.getString("NonLinearImageFilter.jLabelProcessors.text") + threadNumber);
        jLabelBitNumber.setText(bundle.getString("NonLinearImageFilter.jLabelBitNumber.text") + imageParam.bitNumber);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jPanelParam = new javax.swing.JPanel();
        jLabelDiffCoef = new javax.swing.JLabel();
        jTextFieldDiffCoef = new javax.swing.JTextField();
        jLabelNonlinear = new javax.swing.JLabel();
        jTextFieldNonlinear = new javax.swing.JTextField();
        jLabelNSteps = new javax.swing.JLabel();
        jTextFieldNSteps = new javax.swing.JTextField();
        jPanelAction = new javax.swing.JPanel();
        jButtonImage = new javax.swing.JButton();
        jButtonStart = new javax.swing.JButton();
        jPanelSpace = new javax.swing.JPanel();
        jProgressBar = new javax.swing.JProgressBar();
        jCheckBoxNonLinear = new javax.swing.JCheckBox();
        jPanelResults = new javax.swing.JPanel();
        jPanelImages = new javax.swing.JPanel();
        jPanelControls = new javax.swing.JPanel();
        jSliderImages = new javax.swing.JSlider();
        jPanelStatus = new javax.swing.JPanel();
        jLabelBitNumber = new javax.swing.JLabel();
        jLabelProcessors = new javax.swing.JLabel();
        jLabelThreads = new javax.swing.JLabel();
        jLabelExcTime = new javax.swing.JLabel();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemSaveImage = new javax.swing.JMenuItem();
        jMenuItemSaveVideo = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuOptions = new javax.swing.JMenu();
        jMenuItemImageOptions = new javax.swing.JMenuItem();
        jMenuItemFilterOptions = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuLookAndFeel = new javax.swing.JMenu();
        jRadioButtonMenuItemDefault = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemSystem = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemNimbus = new javax.swing.JRadioButtonMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemHelp = new javax.swing.JMenuItem();
        jMenuItemAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("NonLinearImageFilter/Bundle"); // NOI18N
        setTitle(bundle.getString("NonLinearImageFilter.title")); // NOI18N
        setPreferredSize(new java.awt.Dimension(900, 660));

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setDoubleBuffered(true);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(100, 539));

        jPanel2.setPreferredSize(new java.awt.Dimension(779, 585));
        jPanel2.setVerifyInputWhenFocusTarget(false);

        jPanelParam.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), bundle.getString("NonLinearImageFilter.jPanelParam.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION)); // NOI18N
        jPanelParam.setMinimumSize(new java.awt.Dimension(100, 116));

        jLabelDiffCoef.setText(bundle.getString("NonLinearImageFilter.jLabelDiffCoef.text")); // NOI18N

        jTextFieldDiffCoef.setText("0.3");
        jTextFieldDiffCoef.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldDiffCoefFocusLost(evt);
            }
        });
        jTextFieldDiffCoef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDiffCoefActionPerformed(evt);
            }
        });

        jLabelNonlinear.setText(bundle.getString("NonLinearImageFilter.jLabelNonlinear.text")); // NOI18N

        jTextFieldNonlinear.setText(bundle.getString("NonLinearImageFilter.jTextFieldNonlinear.text")); // NOI18N
        jTextFieldNonlinear.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldNonlinearFocusLost(evt);
            }
        });
        jTextFieldNonlinear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldNonlinearActionPerformed(evt);
            }
        });

        jLabelNSteps.setText(bundle.getString("NonLinearImageFilter.jLabelNSteps.text")); // NOI18N

        jTextFieldNSteps.setText("10");
        jTextFieldNSteps.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldNStepsFocusLost(evt);
            }
        });
        jTextFieldNSteps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldNStepsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelParamLayout = new javax.swing.GroupLayout(jPanelParam);
        jPanelParam.setLayout(jPanelParamLayout);
        jPanelParamLayout.setHorizontalGroup(
            jPanelParamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParamLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelParamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelParamLayout.createSequentialGroup()
                        .addGroup(jPanelParamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabelNonlinear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabelDiffCoef, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelParamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextFieldDiffCoef, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                            .addComponent(jTextFieldNonlinear)))
                    .addGroup(jPanelParamLayout.createSequentialGroup()
                        .addComponent(jLabelNSteps, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jTextFieldNSteps, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        jPanelParamLayout.setVerticalGroup(
            jPanelParamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelParamLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelParamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelDiffCoef)
                    .addComponent(jTextFieldDiffCoef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelParamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldNonlinear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelNonlinear))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelParamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldNSteps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelNSteps, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelAction.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), bundle.getString("NonLinearImageFilter.jPanelAction.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION)); // NOI18N
        jPanelAction.setMinimumSize(new java.awt.Dimension(100, 116));

        jButtonImage.setText(bundle.getString("NonLinearImageFilter.jButtonImage.text")); // NOI18N
        jButtonImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonImageActionPerformed(evt);
            }
        });

        jButtonStart.setText(bundle.getString("NonLinearImageFilter.jButtonStart.text")); // NOI18N
        jButtonStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStartActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelActionLayout = new javax.swing.GroupLayout(jPanelAction);
        jPanelAction.setLayout(jPanelActionLayout);
        jPanelActionLayout.setHorizontalGroup(
            jPanelActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelActionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonImage, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonStart, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanelActionLayout.setVerticalGroup(
            jPanelActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelActionLayout.createSequentialGroup()
                .addComponent(jButtonImage, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanelSpace.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), bundle.getString("NonLinearImageFilter.jPanelSpace.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION)); // NOI18N
        jPanelSpace.setMinimumSize(new java.awt.Dimension(100, 116));

        jCheckBoxNonLinear.setText(bundle.getString("NonLinearImageFilter.jCheckBoxNonLinear.text")); // NOI18N
        jCheckBoxNonLinear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNonLinearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelSpaceLayout = new javax.swing.GroupLayout(jPanelSpace);
        jPanelSpace.setLayout(jPanelSpaceLayout);
        jPanelSpaceLayout.setHorizontalGroup(
            jPanelSpaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSpaceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSpaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                    .addComponent(jCheckBoxNonLinear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelSpaceLayout.setVerticalGroup(
            jPanelSpaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSpaceLayout.createSequentialGroup()
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jCheckBoxNonLinear)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelResults.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jPanelImages.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), bundle.getString("NonLinearImageFilter.jPanelImages.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION)); // NOI18N

        javax.swing.GroupLayout jPanelImagesLayout = new javax.swing.GroupLayout(jPanelImages);
        jPanelImages.setLayout(jPanelImagesLayout);
        jPanelImagesLayout.setHorizontalGroup(
            jPanelImagesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 606, Short.MAX_VALUE)
        );
        jPanelImagesLayout.setVerticalGroup(
            jPanelImagesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanelControls.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), bundle.getString("NonLinearImageFilter.jPanelControls.border.title"), javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION)); // NOI18N

        jSliderImages.setOrientation(javax.swing.JSlider.VERTICAL);
        jSliderImages.setEnabled(false);
        jSliderImages.setMaximumSize(new java.awt.Dimension(100, 32767));
        jSliderImages.setMinimumSize(new java.awt.Dimension(30, 36));
        jSliderImages.setPreferredSize(new java.awt.Dimension(30, 200));
        jSliderImages.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderImagesStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanelControlsLayout = new javax.swing.GroupLayout(jPanelControls);
        jPanelControls.setLayout(jPanelControlsLayout);
        jPanelControlsLayout.setHorizontalGroup(
            jPanelControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelControlsLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jSliderImages, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanelControlsLayout.setVerticalGroup(
            jPanelControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelControlsLayout.createSequentialGroup()
                .addComponent(jSliderImages, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanelResultsLayout = new javax.swing.GroupLayout(jPanelResults);
        jPanelResults.setLayout(jPanelResultsLayout);
        jPanelResultsLayout.setHorizontalGroup(
            jPanelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelResultsLayout.createSequentialGroup()
                .addComponent(jPanelImages, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelControls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );
        jPanelResultsLayout.setVerticalGroup(
            jPanelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelResultsLayout.createSequentialGroup()
                .addGroup(jPanelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelControls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelImages, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 47, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelResults, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanelParam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelSpace, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(33, 33, 33))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanelParam, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelAction, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelSpace, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelResults, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        jPanelStatus.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanelStatus.setPreferredSize(new java.awt.Dimension(769, 22));
        jPanelStatus.setLayout(new java.awt.GridLayout(1, 0));

        jLabelBitNumber.setText(bundle.getString("NonLinearImageFilter.jLabelBitNumber.text")); // NOI18N
        jPanelStatus.add(jLabelBitNumber);

        jLabelProcessors.setText(bundle.getString("NonLinearImageFilter.jLabelProcessors.text")); // NOI18N
        jPanelStatus.add(jLabelProcessors);

        jLabelThreads.setText(bundle.getString("NonLinearImageFilter.jLabelThreads.text")); // NOI18N
        jPanelStatus.add(jLabelThreads);

        jLabelExcTime.setText(bundle.getString("NonLinearImageFilter.jLabelExcTime.text")); // NOI18N
        jPanelStatus.add(jLabelExcTime);

        jMenuFile.setText(bundle.getString("NonLinearImageFilter.jMenuFile.text")); // NOI18N

        jMenuItemSaveImage.setText(bundle.getString("NonLinearImageFilter.jMenuItemSaveImage.text")); // NOI18N
        jMenuItemSaveImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveImageActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSaveImage);

        jMenuItemSaveVideo.setText(bundle.getString("NonLinearImageFilter.jMenuItemSaveVideo.text")); // NOI18N
        jMenuItemSaveVideo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveVideoActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSaveVideo);
        jMenuFile.add(jSeparator1);

        jMenuItemExit.setText(bundle.getString("NonLinearImageFilter.jMenuItemExit.text")); // NOI18N
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar.add(jMenuFile);

        jMenuOptions.setText(bundle.getString("NonLinearImageFilter.jMenuOptions.text")); // NOI18N

        jMenuItemImageOptions.setText(bundle.getString("NonLinearImageFilter.jMenuItemImageOptions.text")); // NOI18N
        jMenuItemImageOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemImageOptionsActionPerformed(evt);
            }
        });
        jMenuOptions.add(jMenuItemImageOptions);

        jMenuItemFilterOptions.setText(bundle.getString("NonLinearImageFilter.jMenuItemFilterOptions.text")); // NOI18N
        jMenuItemFilterOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFilterOptionsActionPerformed(evt);
            }
        });
        jMenuOptions.add(jMenuItemFilterOptions);
        jMenuOptions.add(jSeparator2);

        jMenuLookAndFeel.setText(bundle.getString("NonLinearImageFilter.jMenuLookAndFeel.text")); // NOI18N

        jRadioButtonMenuItemDefault.setText(bundle.getString("NonLinearImageFilter.jRadioButtonMenuItemDefault.text")); // NOI18N
        jRadioButtonMenuItemDefault.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonMenuItemDefaultItemStateChanged(evt);
            }
        });
        jMenuLookAndFeel.add(jRadioButtonMenuItemDefault);

        jRadioButtonMenuItemSystem.setText(bundle.getString("NonLinearImageFilter.jRadioButtonMenuItemSystem.text")); // NOI18N
        jRadioButtonMenuItemSystem.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonMenuItemSystemItemStateChanged(evt);
            }
        });
        jMenuLookAndFeel.add(jRadioButtonMenuItemSystem);

        jRadioButtonMenuItemNimbus.setSelected(true);
        jRadioButtonMenuItemNimbus.setText(bundle.getString("NonLinearImageFilter.jRadioButtonMenuItemNimbus.text")); // NOI18N
        jRadioButtonMenuItemNimbus.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonMenuItemNimbusItemStateChanged(evt);
            }
        });
        jMenuLookAndFeel.add(jRadioButtonMenuItemNimbus);

        jMenuOptions.add(jMenuLookAndFeel);

        jMenuBar.add(jMenuOptions);

        jMenuHelp.setText(bundle.getString("NonLinearImageFilter.jMenuHelp.text")); // NOI18N

        jMenuItemHelp.setText(bundle.getString("NonLinearImageFilter.jMenuItemHelp.text")); // NOI18N
        jMenuItemHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHelpActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemHelp);

        jMenuItemAbout.setText(bundle.getString("NonLinearImageFilter.jMenuItemAbout.text")); // NOI18N
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemAbout);

        jMenuBar.add(jMenuHelp);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(jPanelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldDiffCoefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDiffCoefActionPerformed
        // TODO add your handling code here:
        diffCoef = MyTextUtilities.TestValueWithMemory(0.0, 10, jTextFieldDiffCoef,
                "0.3", defaults);
    }//GEN-LAST:event_jTextFieldDiffCoefActionPerformed
    /**
     * Main code
     *
     * @param evt
     */
    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartActionPerformed
        // TODO add your handling code here:
        if (working == true) {
            worker.cancel(true);
            return;
        }
        jProgressBar.setValue(0);
        jProgressBar.setStringPainted(true);
        working = true;
        comp = new CrankNicholson2D(new double[]{-1, 0, 1}, diffCoef, nonLinearCoef,
                precision, anisotropy, threadNumber, iterationCoefficient,
                funcs[funcBox.getSelectedIndex()]);
        jButtonStart.setText(bundle.getString("NonLinearImageFilter.jButtonStart.alttext"));
        jButtonImage.setEnabled(false);
        worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                long t1 = System.nanoTime();
                for (int i = 0; i < nSteps; i++) {
                    // If canceled interrupt the thread
                    if (isCancelled()) {
                        return null;
                    }
                    double[][] currentData;
                    /* Linear or non-linear filtering depending on user choice */
                    currentData = nonLinearFlag ? comp.solveNonLinear(dataList.get(dataList.size() - 1))
                            : comp.solveLinear(dataList.get(dataList.size() - 1));
                    updateUI(currentData, i);
                    dataList.add(currentData);
                }
                // Updating execution time estimate
                execTimeUpdate(System.nanoTime() - t1);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | CancellationException e) {

                } catch (ExecutionException e) {
                    if (e.getCause() instanceof CloneNotSupportedException) {
                        Logger.getLogger(NonLinearImageFilter.class.getName()).log(Level.SEVERE, null, e);
                        return;
                    }
                    if (e.getCause() instanceof Exception) {
                        JOptionPane.showMessageDialog(null, bundle.getString("ERROR DIALOG"), bundle.getString("ERROR DIALOG"), JOptionPane.ERROR_MESSAGE);
                        Logger.getLogger(NonLinearImageFilter.class.getName()).log(Level.SEVERE, null, e);
                        return;
                    }
                }
                updateImagePanel((int) (sliderposition * (imageList.size() - 1) / 100.0));
                working = false;
                jButtonStart.setText(bundle.getString("NonLinearImageFilter.jButtonStart.text"));
                jButtonImage.setEnabled(true);
                comp.shutDown();
            }

            /**
             * Creating the next image in the sequence, updating progress bar
             * and displaying the last image
             *
             * @param data
             */
            public void updateUI(double[][] data, int i) {
                SwingUtilities.invokeLater(() -> {
                    imageList.add(new ImageComponent(data, ((ImageComponent) imageList.get(0)).getImage().getColorModel()));
                    updateImagePanel(imageList.size() - 1);
                    jProgressBar.setValue((int) (100.0 * (i + 1) / nSteps));
                });
            }

            /**
             * Updating task execution time
             *
             * @param time
             */
            public void execTimeUpdate(final long time) {
                SwingUtilities.invokeLater(()
                        -> jLabelExcTime.setText(bundle.getString("NonLinearImageFilter.jLabelExcTime.text") + time
                                + " " + bundle.getString("NANOSECONDS")));
            }
        };
        worker.execute();
    }//GEN-LAST:event_jButtonStartActionPerformed

    private void jButtonImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImageActionPerformed
        // Defining JComponent
        JComponent component = null;
        jButtonStart.setEnabled(false);
        jButtonImage.setEnabled(false);
        jSliderImages.setEnabled(false);
        /*
         * create a button group to chose the source of initial image
         */
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton button1 = new JRadioButton(bundle.getString("GENERATE OPTION"));
        button1.setSelected(true);
        JRadioButton button2 = new JRadioButton(bundle.getString("FROM FILE OPTION"));
        buttonGroup.add(button1);
        buttonGroup.add(button2);
        JPanel panel = new JPanel();
        panel.add(button1);
        panel.add(button2);
        /*
         * Display option window
         */
        Object[] message = {panel};
        int option = JOptionPane.showConfirmDialog(null, message, bundle.getString("IMAGE SOURCE DIALOG TITLE"), JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            /*
             * if OK, proceed to generate/load image
             */
            if (button2.isSelected()) {
                /*
                 * if the second choice, load image from file
                 */
                JFileChooser fo = new JFileChooser(imageRFile);
                fo.setDialogTitle(bundle.getString("IMAGE LOAD DIALOG TITLE"));
                for (FileFilter filter : filters) {
                    fo.addChoosableFileFilter(filter);
                }
                fo.addChoosableFileFilter(new FileNameExtensionFilter("jpg/jpeg", "jpg", "jpeg"));
                fo.setAcceptAllFileFilterUsed(false);

                int ans = fo.showOpenDialog(this);
                if (ans == JFileChooser.APPROVE_OPTION) {
                    try {
                        imageRFile = fo.getSelectedFile();
                        BufferedImage image = ImageIO.read(imageRFile);
                        if (image.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY || 
                                image.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_RGB) {
                            component = new ImageComponent(image, imageParam.bitNumber);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    bundle.getString("NOTGRAYSCALE DIALOG"),
                                    bundle.getString("NOTGRAYSCALE DIALOG TITLE"), JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,
                                bundle.getString("IO ERROR DIALOG"),
                                bundle.getString("IO ERROR DIALOG TITLE"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                /*
                 * if the first choice, generate image
                 */
                component = new ImageComponent(imageParam);
            }
            if (component != null) {
                imageList = new ArrayList<>();
                dataList = new ArrayList<>();
                imageList.add(component);
                dataList.add(((ImageComponent) component).getPixelData());
                updateImagePanel(0);
            }
        }
        if (imageList.size() > 0) {
            jButtonStart.setEnabled(true);
            jSliderImages.setEnabled(true);
        }
        jButtonImage.setEnabled(true);

    }//GEN-LAST:event_jButtonImageActionPerformed

    private void jTextFieldNonlinearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldNonlinearActionPerformed
        // TODO add your handling code here:
        nonLinearCoef = MyTextUtilities.TestValueWithMemory(1, Math.pow(2, 32) - 1, jTextFieldNonlinear,
                "1e4", defaults);
    }//GEN-LAST:event_jTextFieldNonlinearActionPerformed

    private void jTextFieldNStepsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldNStepsActionPerformed
        // TODO add your handling code here:
        nSteps = (int) Math.round(MyTextUtilities.TestValueWithMemory(1, 1000, jTextFieldNSteps,
                "10", defaults));
    }//GEN-LAST:event_jTextFieldNStepsActionPerformed

    private void jTextFieldDiffCoefFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDiffCoefFocusLost
        // TODO add your handling code here:
        diffCoef = MyTextUtilities.TestValueWithMemory(0.0, 10, jTextFieldDiffCoef,
                "0.3", defaults);
    }//GEN-LAST:event_jTextFieldDiffCoefFocusLost

    private void jTextFieldNonlinearFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldNonlinearFocusLost
        // TODO add your handling code here:
        nonLinearCoef = MyTextUtilities.TestValueWithMemory(1, Math.pow(2, 32) - 1, jTextFieldNonlinear,
                "1e4", defaults);
    }//GEN-LAST:event_jTextFieldNonlinearFocusLost

    private void jTextFieldNStepsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldNStepsFocusLost
        // TODO add your handling code here:
        nSteps = (int) Math.round(MyTextUtilities.TestValueWithMemory(1, 1000, jTextFieldNSteps,
                "10", defaults));
    }//GEN-LAST:event_jTextFieldNStepsFocusLost

    private void jSliderImagesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderImagesStateChanged
        // Going over the image sequence
        JSlider source = (JSlider) evt.getSource();
        if (!source.getValueIsAdjusting()) {
            sliderposition = source.getValue();
            updateImagePanel((int) (sliderposition * (imageList.size() - 1) / 100.0));
        }
    }//GEN-LAST:event_jSliderImagesStateChanged

    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
        // Extracting the build date from the MANIFEST.MF file
        Package pk = Package.getPackage("NonLinearImageFilter");
        Date dt = new Date();
        DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Enumeration<URL> mfs = this.getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (mfs.hasMoreElements()) {
                Manifest mft = new Manifest(mfs.nextElement().openStream());
                if (mft.getMainAttributes().getValue("Built-Date") != null) {
                    dt = dtf.parse(mft.getMainAttributes().getValue("Built-Date"));
                }
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(NonLinearImageFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Extracting vendor and version from the MANIFEST.MF file and showing up About popup window
        JOptionPane.showMessageDialog(null,
                bundle.getString("ABOUT BEGIN") + pk.getImplementationVersion()
                + bundle.getString("ABOUT DATE") + DateFormat.getDateInstance(DateFormat.LONG).format(dt)
                + bundle.getString("ABOUT AUTHOR") + pk.getImplementationVendor()
                + "</html>",
                bundle.getString("ABOUT"), JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItemAboutActionPerformed

    private void jMenuItemHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHelpActionPerformed
        // Creating JTextPane for the help
        JTextPane textArea = new JTextPane();
        //Reading the HTML help file
        try {
            textArea.setPage(NonLinearImageFilter.class.
                    getResource("/nonlinearimagefilterhelp/NonLinearImageFilterHelp.html"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, bundle.getString("IO HELP NOTEXIST DIALOG"), bundle.getString("IO HELP ERROR DIALOG TITLE"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Setting dimensions and non-editable
        textArea.setPreferredSize(new Dimension(600, 400));
        textArea.setEditable(false);
        //Creating scroll pane and showing up help
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().add(textArea, BorderLayout.CENTER);
        Object[] message = {
            bundle.getString("HELP DESCRIPTION"), scrollPane
        };
        JOptionPane.showMessageDialog(null, message, bundle.getString("HELP TITLE"), JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItemHelpActionPerformed

    private void jMenuItemImageOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemImageOptionsActionPerformed
        // Initial image generating options
        Object[] message = {
            bundle.getString("IMAGE WIDTH"), xsizeField,
            bundle.getString("IMAGE HEIGHT"), ysizeField,
            bundle.getString("NOISE"), noiseField,
            bundle.getString("SIGNAL"), signalField,
            bundle.getString("SCALE"), scaleField,
            bundle.getString("BIT_NUMBER"), bitNumberMenu
        };
        int option = JOptionPane.showConfirmDialog(null, message, bundle.getString("IMAGE GENERATOR PARAMETERS DIALOG"),
                JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            imageParam.xsize = (Integer) xsizeField.getValue();
            imageParam.ysize = (Integer) ysizeField.getValue();
            imageParam.noise = (int) Math.pow(2, (Integer) noiseField.getValue());
            imageParam.signal = (int) Math.pow(2, (Integer) signalField.getValue());
            imageParam.scale = (Double) scaleField.getValue();
            imageParam.bitNumber = bitnesses[bitNumberMenu.getSelectedIndex()];
        }
        jLabelBitNumber.setText(bundle.getString("NonLinearImageFilter.jLabelBitNumber.text") + imageParam.bitNumber);
    }//GEN-LAST:event_jMenuItemImageOptionsActionPerformed

    private void jMenuItemSaveVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveVideoActionPerformed
        // Saving the image sequence as an image file
        if (imageList.isEmpty()) {
            return;
        }
        int width = ((ImageComponent) imageList.get(0)).getImage().getWidth();
        int height = ((ImageComponent) imageList.get(0)).getImage().getHeight();
        JPanel saveVideoPanel = new JPanel();
        JPanel innerPanel1 = new JPanel();
        JPanel innerPanel2 = new JPanel();
        saveVideoPanel.setLayout(new BoxLayout(saveVideoPanel, BoxLayout.PAGE_AXIS));
        innerPanel1.add(new JLabel(bundle.getString("FRAME RATE LABEL")));
        innerPanel1.add(frameRateField);
        saveVideoPanel.add(innerPanel1);
        JComboBox<String> box = new JComboBox<>();
        box.addItem(bundle.getString("VIDEO FORMAT 1"));
        box.addItem(bundle.getString("VIDEO FORMAT 2"));
        innerPanel2.add(box);
        saveVideoPanel.add(innerPanel2);
        /*
         * Create file and format choosing dialog
         */
        JFileChooser fo = new JFileChooser(videoWFile);
        fo.setDialogTitle(bundle.getString("VIDEO SAVE DIALOG TITLE"));
        fo.addChoosableFileFilter(new FileNameExtensionFilter("avi", "avi"));
        fo.addChoosableFileFilter(new FileNameExtensionFilter("quicktime", "mov"));
        fo.setAcceptAllFileFilterUsed(false);
        fo.setAccessory(saveVideoPanel);
        int ans = fo.showSaveDialog(this);
        /*
         * Saving uncompressed avi or QuickTime video
         */
        if (ans == JFileChooser.APPROVE_OPTION) {
            frameRate = (Integer) frameRateField.getValue();
            videoFormat = box.getSelectedIndex();
            try {
                videoWFile = fo.getSelectedFile();
                MediaLocator mc = new MediaLocator(videoWFile.toURL());
                ImagesToMovie imageToMovie = new ImagesToMovie();
                imageToMovie.doIt(width, height, frameRate, imageList, mc, videoFormat);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        bundle.getString("IO ERROR DIALOG TITLE"),
                        bundle.getString("IO ERROR DIALOG"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItemSaveVideoActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        // Exiting
        System.exit(0);
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jMenuItemFilterOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFilterOptionsActionPerformed
        // Displaying filtering numerical options
        Object[] message = {
            bundle.getString("NonLinearImageFilter.jTextFieldPrecision.text"), precisionField,
            bundle.getString("NonLinearImageFilter.jTextFieldAnisotropy.text"), anisotropyField,
            bundle.getString("NonLinearImageFilter.jTextFieldThreadNumber.text"), threadNumberField,
            bundle.getString("NonLinearImageFilter.jTextFieldIterCoef.text"), iterField,
            bundle.getString("NonLinearImageFilter.jComboBoxFunc.text"), funcBox
        };
        int option = JOptionPane.showConfirmDialog(null, message,
                bundle.getString("NonLinearImageFilter.FilterOptions.title"), JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            precision = (Double) precisionField.getValue();
            anisotropy = (Double) anisotropyField.getValue();
            threadNumber = (Integer) threadNumberField.getValue();
            iterationCoefficient = (Double) iterField.getValue();
            jLabelThreads.setText(bundle.getString("NonLinearImageFilter.jLabelThreads.text") + threadNumber);
        }
    }//GEN-LAST:event_jMenuItemFilterOptionsActionPerformed

    private void jCheckBoxNonLinearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNonLinearActionPerformed
        // Linear or non-linear
        nonLinearFlag = jCheckBoxNonLinear.isSelected();
    }//GEN-LAST:event_jCheckBoxNonLinearActionPerformed

    private void jMenuItemSaveImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveImageActionPerformed
        // Saving as an image file
        if (imageList.isEmpty()) {
            return;
        }
        JFileChooser fo = new JFileChooser(imageWFile);
        fo.setDialogTitle(bundle.getString("IMAGE SAVE DIALOG TITLE"));
        for (FileFilter filter : filters) {
            fo.addChoosableFileFilter(filter);
        }
        fo.setAcceptAllFileFilterUsed(false);
        int ans = fo.showSaveDialog(this);

        if (ans == JFileChooser.APPROVE_OPTION) {
            try {
                imageWFile = fo.getSelectedFile();
                int index = (int) (sliderposition * (imageList.size() - 1) / 100.0);
                String type = ((FileNameExtensionFilter) fo.getFileFilter()).getExtensions()[0];
                BufferedImage image = ((ImageComponent) imageList.get(index)).getImage();
                if (image.getData().getTransferType() == DataBuffer.TYPE_INT) {
                    int xsize = image.getWidth(null);
                    int ysize = image.getHeight(null);
                    double[] pixels = new double[xsize * ysize];
                    //Creating a new color model for the float transfer type for TYPE_INT
                    ColorModel cm = new ImageComponent.Int32ComponentColorModel(image.getColorModel().getColorSpace(),
                            new int[]{32}, false, true, Transparency.OPAQUE, DataBuffer.TYPE_FLOAT);
                    /*
                     * Create an Writableraster from the existing color model and fill it with pixels
                     */
                    double[][] ar = dataList.get(index);
                    for (int i = 0; i < ysize - 1; i++) {
                        System.arraycopy(ar[i], 0, pixels, i * xsize, xsize);
                    }
                    WritableRaster raster = cm.createCompatibleWritableRaster(xsize, ysize);
                    raster.setPixels(0, 0, xsize, ysize, pixels);
                    /*
                     * Create a BufferedImage from the raster and color model and return it
                     */
                    image = new BufferedImage(cm, raster, true, null);
                }
                ImageIO.write(image, type, imageWFile);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        bundle.getString("IO SAVE ERROR DIALOG TITLE"),
                        bundle.getString("IO ERROR DIALOG"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItemSaveImageActionPerformed

    private void jRadioButtonMenuItemDefaultItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemDefaultItemStateChanged
        // Switching to the Default Look and Feel
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Logger.getLogger(NonLinearImageFilter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jRadioButtonMenuItemDefaultItemStateChanged

    private void jRadioButtonMenuItemSystemItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemSystemItemStateChanged
        // Switching to the System Look and Feel
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Logger.getLogger(NonLinearImageFilter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jRadioButtonMenuItemSystemItemStateChanged

    private void jRadioButtonMenuItemNimbusItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemNimbusItemStateChanged
        // Switching to the Nimbus Look and Feel
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Logger.getLogger(NonLinearImageFilter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jRadioButtonMenuItemNimbusItemStateChanged

    private void jComboBoxBitNumberActionPerformed(java.awt.event.ItemEvent evt) {
        // Processing actions from image bitness combobox
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            int bitness = bitnesses[bitNumberMenu.getSelectedIndex()];
            MyTextUtilities.changeIntegerFormattedTextField(noiseField, bitness - 2, 1, bitness - 1);
            MyTextUtilities.changeIntegerFormattedTextField(signalField, bitness - 1, 1, bitness);
            jTextFieldNonlinear.setText(Double.toString(nonLinearCoefs[bitNumberMenu.getSelectedIndex()]));
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NonLinearImageFilter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Setting default locale */
        if (args.length > 0) {
            Locale.setDefault(new Locale(args[0], "US"));
        } else {
            Locale.setDefault(new Locale("en", "US"));
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new NonLinearImageFilter().setVisible(true);
        });
    }

    /**
     * Updating image panel if the image has been changed
     * 
     * @param index 
     */
    private void updateImagePanel(int index) {
        JComponent component = imageList.get(index);
        if (jPanelImages.getComponentCount() == 0) {
            component.setPreferredSize(jPanelImages.getSize());
        } else {
            component.setPreferredSize(jPanelImages.getComponent(0).getPreferredSize());
            jPanelImages.removeAll();
        }
        jPanelImages.setLayout(new BorderLayout(0, 0));
        jPanelImages.add(component, BorderLayout.CENTER);
        ((TitledBorder) jPanelImages.getBorder()).setTitle(bundle.
                getString("NonLinearImageFilter.jPanelImages.border.title") + " - " + index);
        jPanelImages.revalidate();
        jPanelImages.repaint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonImage;
    private javax.swing.JButton jButtonStart;
    private javax.swing.JCheckBox jCheckBoxNonLinear;
    private javax.swing.JLabel jLabelBitNumber;
    private javax.swing.JLabel jLabelDiffCoef;
    private javax.swing.JLabel jLabelExcTime;
    private javax.swing.JLabel jLabelNSteps;
    private javax.swing.JLabel jLabelNonlinear;
    private javax.swing.JLabel jLabelProcessors;
    private javax.swing.JLabel jLabelThreads;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemFilterOptions;
    private javax.swing.JMenuItem jMenuItemHelp;
    private javax.swing.JMenuItem jMenuItemImageOptions;
    private javax.swing.JMenuItem jMenuItemSaveImage;
    private javax.swing.JMenuItem jMenuItemSaveVideo;
    private javax.swing.JMenu jMenuLookAndFeel;
    private javax.swing.JMenu jMenuOptions;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelAction;
    private javax.swing.JPanel jPanelControls;
    private javax.swing.JPanel jPanelImages;
    private javax.swing.JPanel jPanelParam;
    private javax.swing.JPanel jPanelResults;
    private javax.swing.JPanel jPanelSpace;
    private javax.swing.JPanel jPanelStatus;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemDefault;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemNimbus;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemSystem;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSlider jSliderImages;
    private javax.swing.JTextField jTextFieldDiffCoef;
    private javax.swing.JTextField jTextFieldNSteps;
    private javax.swing.JTextField jTextFieldNonlinear;
    // End of variables declaration//GEN-END:variables
}
