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
import static NonLinearImageFilter.ImageComponent.CS;
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
import java.io.BufferedReader;
import java.io.EOFException;
import javax.media.MediaLocator;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.IntStream;
import static java.util.Comparator.comparingDouble;

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
import javax.swing.event.ChangeEvent;

/**
 *
 * @author Ruslan Feshchenko
 * @version 3.0b
 */
public class NonLinearImageFilter extends javax.swing.JFrame {

    /**
     * Creates new form NonLinearImageFilter
     */
    private final ImageParam imageParam;
    private ArrayList<JComponent> imageList;
    private ImageComponent iImage = null;
    private int nSteps = 10, threadNumber, sliderposition = 50, columnNumber = 7;
    private double precision = 1e-10, diffCoef = 0.01, nonLinearCoef = 10000,
            anisotropy = 0, iterationCoefficient = 0.5;
    private boolean nonLinearFlag = false, working = false, maskworking = false;
    private CrankNicholson2D comp;
    private final Map defaults;
    private SwingWorker<Void, Void> worker;
    private ArrayList<double[][]> dataList;
    private double[][] maskdata;
    private final JFormattedTextField xsizeField, ysizeField, noiseField, signalField,
            scaleField, precisionField, anisotropyField, frameRateField,
            threadNumberField, iterField, columnFiled;
    private final JSlider maskSlider;
    private final JPanel maskpanel, maskpanel1, maskpanel2;
    private final JLabel maskstatlabel1, maskstatlabel2, thresholdlabel;
    private final JComboBox bitNumberMenu;
    private final JComboBox<String> funcBox;
    private final ResourceBundle bundle;
    private final FileFilter[] imagefilters, textfilters;
    private int frameRate = 10, videoFormat = 0;
    private final int[] bitnesses = new int[]{8, 16, 32};
    private final double[] nonLinearCoefs = new double[]{30, 1e4, 1e8};
    private File imageRFile = null, imageWFile = null, videoWFile = null, textWFile = null, textRFile = null;
    private final DoubleFunction[] funcs;
    private static final int NUMPOINTS = 100;

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
        this.columnFiled = MyTextUtilities.getIntegerFormattedTextField(7, 1, 15);
        this.bundle = ResourceBundle.getBundle("NonLinearImageFilter/Bundle");
        //Elements of the mask tool box
        this.maskSlider = new JSlider(JSlider.HORIZONTAL, 0, 65535, 1000);
        this.maskpanel = new JPanel();
        this.maskpanel1 = new JPanel();
        this.maskpanel2 = new JPanel();
        this.maskstatlabel1 = new JLabel();
        this.maskstatlabel2 = new JLabel();
        this.thresholdlabel = new JLabel();

        this.imagefilters = new FileFilter[]{
            new FileNameExtensionFilter("tif/tiff", "tif", "tiff"),
            new FileNameExtensionFilter("png", "png"),
            new FileNameExtensionFilter("gif", "gif")
        };
        this.textfilters = new FileFilter[]{
            new FileNameExtensionFilter("dat/data", "dat", "data"),
            new FileNameExtensionFilter("txt/text", "txt", "text")
        };
        this.funcs = new DoubleFunction[]{p -> 1 / (1 + p), p -> Math.exp(-p)};
        this.funcBox = new JComboBox<>();
        this.funcBox.addItem(bundle.getString("FUNCTION 1"));
        this.funcBox.addItem(bundle.getString("FUNCTION 2"));

        this.bitNumberMenu = new JComboBox(new String[]{"8 bit", "16 bit", "32 bit"});
        bitNumberMenu.setSelectedIndex(1);
        bitNumberMenu.addItemListener((java.awt.event.ItemEvent evt) -> {
            jComboBoxBitNumberActionPerformed(evt);
        });

        initComponents();
        //Disabling saving image commands and start until the images are created
        disableElemtnts();
        jButtonImage.setEnabled(true);
        //Setting a listerner for style changes
        UIManager.addPropertyChangeListener(e -> SwingUtilities.updateComponentTreeUI(this));
        //Setting radio buttons for style selection
        ButtonGroup LFGroup = new ButtonGroup();
        LFGroup.add(jRadioButtonMenuItemDefault);
        LFGroup.add(jRadioButtonMenuItemSystem);
        LFGroup.add(jRadioButtonMenuItemNimbus);
        //Setting status line text
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
        jButtonSegment = new javax.swing.JButton();
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
        jMenuItemLoadImageText = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSaveImage = new javax.swing.JMenuItem();
        jMenuItemSaveImageText = new javax.swing.JMenuItem();
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
        jMenuTools = new javax.swing.JMenu();
        jMenuItemSegment = new javax.swing.JMenuItem();
        jMenuItemLoadSegment = new javax.swing.JMenuItem();
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

        jButtonSegment.setText(bundle.getString("NonLinearImageFilter.jButtonSegment.text")); // NOI18N
        jButtonSegment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSegmentActionPerformed(evt);
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
                    .addGroup(jPanelSpaceLayout.createSequentialGroup()
                        .addComponent(jCheckBoxNonLinear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSegment, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanelSpaceLayout.setVerticalGroup(
            jPanelSpaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSpaceLayout.createSequentialGroup()
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelSpaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSegment, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxNonLinear))
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

        jMenuItemLoadImageText.setText(bundle.getString("NonLinearImageFilter.jMenuItemLoadImageText.text")); // NOI18N
        jMenuItemLoadImageText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadImageTextActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemLoadImageText);
        jMenuFile.add(jSeparator3);

        jMenuItemSaveImage.setText(bundle.getString("NonLinearImageFilter.jMenuItemSaveImage.text")); // NOI18N
        jMenuItemSaveImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveImageActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSaveImage);

        jMenuItemSaveImageText.setText(bundle.getString("NonLinearImageFilter.jMenuItemSaveImageText.text")); // NOI18N
        jMenuItemSaveImageText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveImageTextActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSaveImageText);

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

        jMenuTools.setText(bundle.getString("NonLinearImageFilter.jMenuTools.text")); // NOI18N

        jMenuItemSegment.setText(bundle.getString("NonLinearImageFilter.jMenuItemSegment.text")); // NOI18N
        jMenuItemSegment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSegmentActionPerformed(evt);
            }
        });
        jMenuTools.add(jMenuItemSegment);

        jMenuItemLoadSegment.setText(bundle.getString("NonLinearImageFilter.jMenuItemLoadSegment.text")); // NOI18N
        jMenuItemLoadSegment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadSegmentActionPerformed(evt);
            }
        });
        jMenuTools.add(jMenuItemLoadSegment);

        jMenuBar.add(jMenuTools);

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
        //Disabling menus and buttons
        disableElemtnts();
        /*
         * create a button group to chose the source of initial image
         */
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton button1 = new JRadioButton(bundle.getString("GENERATE OPTION"));
        button1.setSelected(true);
        JRadioButton button2 = new JRadioButton(bundle.getString("FROM FILE OPTION"));
        JRadioButton button3 = new JRadioButton(bundle.getString("FROM TEXT FILE OPTION"));
        buttonGroup.add(button1);
        buttonGroup.add(button2);
        buttonGroup.add(button3);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(button1);
        panel.add(button2);
        panel.add(button3);
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
                 * If the second choice, load image from file
                 */
                JFileChooser fo = new JFileChooser(imageRFile);
                fo.setDialogTitle(bundle.getString("IMAGE LOAD DIALOG TITLE"));
                for (FileFilter filter : imagefilters) {
                    fo.addChoosableFileFilter(filter);
                }
                fo.addChoosableFileFilter(new FileNameExtensionFilter("jpg/jpeg", "jpg", "jpeg"));
                fo.setAcceptAllFileFilterUsed(false);

                int ans = fo.showOpenDialog(this);
                if (ans == JFileChooser.APPROVE_OPTION) {
                    try {
                        imageRFile = fo.getSelectedFile();
                        BufferedImage image = ImageIO.read(imageRFile);
                        if (image.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY
                                || image.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_RGB) {
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
            } else if (button3.isSelected()) {
                //If the third choice then get the image from a text file
                jMenuItemLoadImageTextActionPerformed(evt);
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
        //Enabling buttons and menu items
        enableElemtnts();

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
            bundle.getString("BIT_NUMBER"), bitNumberMenu,
            bundle.getString("COLUMN_NUMBER"), columnFiled
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
            columnNumber = (Integer) columnFiled.getValue();
        }
        jLabelBitNumber.setText(bundle.getString("NonLinearImageFilter.jLabelBitNumber.text") + imageParam.bitNumber);
    }//GEN-LAST:event_jMenuItemImageOptionsActionPerformed

    private void jMenuItemSaveVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveVideoActionPerformed
        // Saving the image sequence as a video file
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
        int index = (int) (sliderposition * (imageList.size() - 1) / 100.0);
        BufferedImage image = ((ImageComponent) imageList.get(index)).getImage();
        double[][] ar = dataList.get(index);
        saveImageFile(image, ar);
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

    private void jMenuItemSaveImageTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveImageTextActionPerformed
        // Saving the current image as a text file:
        /*
         * Create text file and format choosing dialog
         */
        JFileChooser fo = new JFileChooser(textWFile != null ? textWFile.getAbsolutePath() : null);
        fo.setDialogTitle(bundle.getString("TEXT SAVE DIALOG TITLE"));
        for (FileFilter filter : textfilters) {
            fo.addChoosableFileFilter(filter);
        }
        fo.setAcceptAllFileFilterUsed(true);
        fo.setAccessory(null);
        fo.setSelectedFile(new File(String.format("%s_%d_%d%s", "image_", imageParam.xsize, imageParam.ysize, ".dat")));
        int ans = fo.showSaveDialog(this);
        /*
         * Saving uncompressed avi or QuickTime video
         */
        if (ans == JFileChooser.APPROVE_OPTION) {
            textWFile = fo.getSelectedFile(); //Getting the text file handle
            Formatter fm = new Formatter(); //Creating a formater for text output
            double[][] data = dataList.get((int) (sliderposition * (imageList.size() - 1) / 100.0));
            try (PrintWriter stream
                    = new PrintWriter(new FileWriter(textWFile, false))) {
                for (int i = 0; i < imageParam.ysize; i++) {
                    for (int j = 0; j < imageParam.xsize; j++) {
                        fm.format(Locale.US, "%d %d %.10f%n", i, j, data[i][j]);
                    }
                }
                ((PrintWriter) stream).println(fm);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        bundle.getString("IO ERROR DIALOG"),
                        bundle.getString("IO ERROR DIALOG TITLE"), JOptionPane.ERROR_MESSAGE);
            }

        }
    }//GEN-LAST:event_jMenuItemSaveImageTextActionPerformed

    private void jMenuItemLoadImageTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadImageTextActionPerformed
        // Loading an image from a text file
        //Disabling menus and buttons
        disableElemtnts();
        /*
         * Create text file and format choosing dialog
         */
        JFileChooser fo = new JFileChooser(textRFile);
        fo.setDialogTitle(bundle.getString("IMAGE TEXT LOAD DIALOG TITLE"));
        //Adding text file imagefilters
        for (FileFilter filter : textfilters) {
            fo.addChoosableFileFilter(filter);
        }
        fo.setAcceptAllFileFilterUsed(true);

        int ans = fo.showOpenDialog(this);
        if (ans == JFileChooser.APPROVE_OPTION) {
            //Creating the data array
            double[][] pixelData = null;
            //Getting selected file
            textRFile = fo.getSelectedFile();
            //Calling the image reading function
            pixelData = readTextImageFile(textRFile, columnNumber, imageParam);
            //Creating an image from loaded data
            ImageComponent ic = new ImageComponent(pixelData,
                    new ImageComponent.Int32ComponentColorModel(CS, new int[]{imageParam.bitNumber},
                            false, true, Transparency.OPAQUE,
                            ImageComponent.initializeDataBufferType(imageParam.bitNumber)));
            imageList = new ArrayList<>();
            dataList = new ArrayList<>();
            imageList.add(ic);
            dataList.add(pixelData);
            updateImagePanel(0);
            //Enabling buttons and menu items
            enableElemtnts();
        }
    }//GEN-LAST:event_jMenuItemLoadImageTextActionPerformed

    private void jButtonSegmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSegmentActionPerformed
        // Displaying mask selection box:
        //Getting the maximum pixel value
        int maxImageValue = ((ImageComponent) imageList
                .get((int) (sliderposition * (imageList.size() - 1) / 100.0))).getMaxValue();
        //Setting up the mask slider
        maskSlider.setMaximum((int) Math.pow(10, (int) Math.log10(maxImageValue) + 1));
        maskSlider.setMajorTickSpacing(((int) Math.pow(10, (int) Math.log10(maxImageValue))) * 5);
        maskSlider.setPaintTicks(true);
        maskSlider.setPaintLabels(true);
        maskSlider.setValue(maxImageValue / 2);
        maskSlider.addChangeListener(this::maskSliderChanged);

        //Disabling segmentation button and menu
        maskSlider.setEnabled(false);
        jMenuItemSegment.setEnabled(false);
        jButtonSegment.setEnabled(false);

        //Setting up parameters
        maskworking = true;
        int[] retpos = {maskSlider.getMaximum() - 1};
        double[][] data0 = dataList.get((int) (sliderposition * (imageList.size() - 1) / 100.0));

        //Calculating the optimal value in a separate thread
        SwingWorker<Double, Void> maskworker = new SwingWorker<Double, Void>() {

            @Override
            protected Double doInBackground() throws Exception {
                return findOptimalThreshold(data0, retpos[0]);
            }

            @Override
            protected void done() {
                try {
                    retpos[0] = (int) Math.round(get());
                } catch (InterruptedException | CancellationException e) {

                } catch (ExecutionException e) {
                    JOptionPane.showMessageDialog(null, bundle.getString("ERROR DIALOG"), bundle.getString("ERROR DIALOG"), JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(NonLinearImageFilter.class.getName()).log(Level.SEVERE, null, e);
                    return;
                }
                maskworking = false;
                //Enabling segmentation buttonand menu
                jButtonSegment.setEnabled(true);
                jMenuItemSegment.setEnabled(true);
                maskSlider.setValue(retpos[0]);
                maskSlider.setEnabled(true);

                //Setting up images
                updateMaskPanel(retpos[0]);

                Object[] message = {
                    bundle.getString("NonLinearImageFilter.jMaskSlider.text"), maskSlider,
                    bundle.getString("NonLinearImageFilter.jMaskPanel.text"), maskpanel,
                    bundle.getString("NonLinearImageFilter.jMaskPanel1.text"), maskpanel1,
                    bundle.getString("NonLinearImageFilter.jMaskPanel2.text"), maskpanel2,
                    bundle.getString("NonLinearImageFilter.jThresholdLable.text"), thresholdlabel, 
                    bundle.getString("NonLinearImageFilter.jMaskStatLabel1.text"), maskstatlabel2,
                    bundle.getString("NonLinearImageFilter.jMaskStatLabel2.text"), maskstatlabel1};

                int option = JOptionPane.showConfirmDialog(null, message,
                        bundle.getString("NonLinearImageFilter.MaskOptions.title"), JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                if (option == JOptionPane.OK_OPTION) {
                    saveImageFile(iImage.getImage(), maskdata);
                }
                //Removing change listerner from the mask slider
                maskSlider.removeChangeListener(maskSlider.getChangeListeners()[0]);
            }

        };
        //Starting optimization
        maskworker.execute();
    }//GEN-LAST:event_jButtonSegmentActionPerformed

    private void jMenuItemSegmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSegmentActionPerformed
        // Segment the current image:
        jButtonSegmentActionPerformed(evt);
    }//GEN-LAST:event_jMenuItemSegmentActionPerformed

    private void jMenuItemLoadSegmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadSegmentActionPerformed
        // Load and segment the image:
        /*
         * Create text file and format choosing dialog
         */
        JFileChooser fo = new JFileChooser(textRFile);
        fo.setDialogTitle(bundle.getString("IMAGE TEXT LOAD DIALOG TITLE"));
        //Adding text file imagefilters
        for (FileFilter filter : textfilters) {
            fo.addChoosableFileFilter(filter);
        }
        fo.setAcceptAllFileFilterUsed(true);

        int ans = fo.showOpenDialog(this);
        if (ans == JFileChooser.APPROVE_OPTION) {
            //Creating the data array
            double[][] pixelData = null;
            //Getting selected file
            textRFile = fo.getSelectedFile();
            //Calling the image reading function
            pixelData = readTextImageFile(textRFile, columnNumber, imageParam);
            //Creating an image from loaded data
            ImageComponent ic = new ImageComponent(pixelData,
                    new ImageComponent.Int32ComponentColorModel(CS, new int[]{imageParam.bitNumber},
                            false, true, Transparency.OPAQUE,
                            ImageComponent.initializeDataBufferType(imageParam.bitNumber)));
            
            Object[] message = {
                    bundle.getString("NonLinearImageFilter.jMaskPanel.text"), maskpanel,
                    bundle.getString("NonLinearImageFilter.jMaskPanel1.text"), maskpanel1,
                    bundle.getString("NonLinearImageFilter.jMaskPanel2.text"), maskpanel2,
                    bundle.getString("NonLinearImageFilter.jThresholdLable.text"), thresholdlabel, 
                    bundle.getString("NonLinearImageFilter.jMaskStatLabel1.text"), maskstatlabel2,
                    bundle.getString("NonLinearImageFilter.jMaskStatLabel2.text"), maskstatlabel1};

                int option = JOptionPane.showConfirmDialog(null, message,
                        bundle.getString("NonLinearImageFilter.MaskOptions.title"), JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                if (option == JOptionPane.OK_OPTION) {
                    saveImageFile(iImage.getImage(), maskdata);
                }
        }
    }//GEN-LAST:event_jMenuItemLoadSegmentActionPerformed

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

    /**
     * Reading the text image file produced by MICA
     *
     * @param file
     * @param column
     * @param ipar
     * @return
     */
    private double[][] readTextImageFile(File file, int column, ImageParam ipar) {
        Scanner scan;
        String line;
        int maxrow = 0, maxcolumn = 0, tmp;
        boolean success = false;
        double[][] pixelData = null;

        //Cycling until reading is succesful
        while (!success) {
            success = true;
            //Creating the data array
            pixelData = new double[ipar.ysize][ipar.xsize];

            //Reading from the file
            try ( //Openning the text stream for read operations
                    BufferedReader stream = new BufferedReader(new FileReader(file))) {
                //Reading the first line
                line = stream.readLine();
                //If the first lines  is empty than the file is empty
                if (line == null | line.isEmpty()) {
                    throw new EOFException("The line is empty or null");
                }
                //Reading the data from the file with prespecified image dimensions
                for (int i = 0; i < ipar.ysize; i++) {
                    for (int j = 0; j < ipar.xsize; j++) {
                        line = stream.readLine();
                        //If no more lines than the file has ended prematurely
                        if (line == null | line.isEmpty()) {
                            throw new EOFException("The line is empty or null");
                        }
                        try {
                            scan = new Scanner(line);
                            scan.useLocale(Locale.US);
                            //Reading column and raw numbers
                            tmp = scan.nextInt();
                            maxrow = tmp > maxrow ? tmp : maxrow;
                            tmp = scan.nextInt();
                            maxcolumn = tmp > maxcolumn ? tmp : maxcolumn;
                            //Reading the pre-specified column
                            for (int k = 0; k < column; k++) {
                                pixelData[i][j] = scan.nextDouble();
                            }
                        } catch (NoSuchElementException e) {
                            throw new IOException(e);
                        }

                    }
                }
                if (maxrow + 1 > ipar.ysize || maxcolumn + 1 > ipar.xsize) {
                    throw new EOFException("The image size is larger than specified");
                }
            } catch (EOFException ex) {
                int answer = JOptionPane.showConfirmDialog(null,
                        bundle.getString("EOF ERROR DIALOG"),
                        bundle.getString("EOF ERROR DIALOG TITLE"), JOptionPane.YES_NO_OPTION);
                success = (answer != JOptionPane.YES_OPTION);
                if (!success) {
                    ipar.ysize = maxrow + 1;
                    ipar.xsize = maxcolumn + 1;
                    xsizeField.setValue(ipar.xsize);
                    ysizeField.setValue(ipar.ysize);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        bundle.getString("IO ERROR DIALOG"),
                        bundle.getString("IO ERROR DIALOG TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        }
        return pixelData;
    }

    /**
     * Enabling buttons and menu items to handle images
     */
    private void enableElemtnts() {
        if (imageList.size() > 0) {
            jButtonStart.setEnabled(true);
            jSliderImages.setEnabled(true);
            jMenuItemSaveImage.setEnabled(true);
            jMenuItemSaveImageText.setEnabled(true);
            jMenuItemSaveVideo.setEnabled(true);
            jMenuItemSegment.setEnabled(true);
            jButtonSegment.setEnabled(true);
        }
        jButtonImage.setEnabled(true);
    }

    /**
     * Disabling buttons and menu items to handle images
     */
    private void disableElemtnts() {
        jButtonStart.setEnabled(false);
        jButtonImage.setEnabled(false);
        jSliderImages.setEnabled(false);
        jMenuItemSaveImage.setEnabled(false);
        jMenuItemSaveImageText.setEnabled(false);
        jMenuItemSaveVideo.setEnabled(false);
        jMenuItemSegment.setEnabled(false);
        jButtonSegment.setEnabled(false);
    }

    /**
     * The listener for the mask slider
     */
    private void maskSliderChanged(ChangeEvent e) {
        // Calling mask panel updating function
        if (!maskSlider.getValueIsAdjusting()) {
            updateMaskPanel(maskSlider.getValue());
        }
    }

    /**
     * Updating mask panel
     *
     * @param pos
     */
    private void updateMaskPanel(double pos) {
        //Setting up images
        maskpanel.removeAll();
        maskpanel1.removeAll();
        maskpanel2.removeAll();

        double average1 = 0, average2 = 0, averagesq1 = 0, averagesq2 = 0, tmp = 0;
        double[][] data0 = dataList.get((int) (sliderposition * (imageList.size() - 1) / 100.0)),
                data1 = new double[data0.length][data0[0].length],
                data2 = new double[data0.length][data0[0].length];
        int counter = 0;

        //Calculating the maxal pixel value of the current image
        int maxImageValue = ((ImageComponent) imageList
                .get((int) (sliderposition * (imageList.size() - 1) / 100.0))).getMaxValue();
        int size = data0.length * data0[0].length;
        maskdata = new double[data0.length][data0[0].length];
        for (int i = 0; i < data0.length; i++) {
            for (int j = 0; j < data0[0].length; j++) {
                tmp = data0[i][j];
                if (data0[i][j] > pos) {
                    maskdata[i][j] = maxImageValue;
                    data1[i][j] = tmp;
                    data2[i][j] = maxImageValue;
                    average1 += tmp;
                    averagesq1 += tmp * tmp;
                    counter++;
                } else {
                    maskdata[i][j] = 0;
                    data1[i][j] = 0;
                    data2[i][j] = tmp;
                    average2 += tmp;
                    averagesq2 += tmp * tmp;
                }
            }
        }
        average1 /= counter;
        average2 /= size - counter;
        averagesq1 /= counter;
        averagesq2 /= size - counter;
        //Color model
        ColorModel cm = new ImageComponent.Int32ComponentColorModel(CS, new int[]{imageParam.bitNumber},
                false, true, Transparency.OPAQUE,
                ImageComponent.initializeDataBufferType(imageParam.bitNumber));
        //Creating and adding three image: mask, intracellular and intercellular
        iImage = new ImageComponent(maskdata, cm);
        JComponent intraImage = new ImageComponent(data1, cm);
        JComponent interImage = new ImageComponent(data2, cm);
        iImage.setPreferredSize(new Dimension(300, 200));
        intraImage.setPreferredSize(new Dimension(300, 200));
        interImage.setPreferredSize(new Dimension(300, 200));
        maskpanel.add(iImage);
        maskpanel1.add(intraImage);
        maskpanel2.add(interImage);
        maskpanel.revalidate();
        maskpanel.repaint();
        maskpanel1.revalidate();
        maskpanel1.repaint();
        maskpanel2.revalidate();
        maskpanel2.repaint();
        thresholdlabel.setText(Double.toString(pos));
        maskstatlabel1.setText((int) Math.round(average2) + " \u00B1 "
                + (int) Math.round(Math.sqrt(averagesq2 - average2 * average2)));
        maskstatlabel2.setText((int) Math.round(average1) + " \u00B1 "
                + (int) Math.round(Math.sqrt(averagesq1 - average1 * average1)));
    }

    /**
     * Looking for the optimal segmentation threshold
     *
     * @param data0
     * @param pos
     * @return
     */
    private double findOptimalThreshold(double[][] data0, double posmax) {
        double average1, average2, averagesq1, averagesq2, tmp, pos = 0, delta = posmax / (NUMPOINTS - 1);
        int counter;
        int size = data0.length * data0[0].length;
        double[] results = new double[NUMPOINTS];
        for (int k = 0; k < NUMPOINTS; k++) {
            counter = 0;
            average1 = 0;
            average2 = 0;
            averagesq1 = 0;
            averagesq2 = 0;
            for (int i = 0; i < data0.length; i++) {
                for (int j = 0; j < data0[0].length; j++) {
                    tmp = data0[i][j];
                    if (data0[i][j] > pos) {
                        average1 += tmp;
                        averagesq1 += tmp * tmp;
                        counter++;
                    } else {
                        average2 += tmp;
                        averagesq2 += tmp * tmp;
                    }
                }
            }
            pos += delta;
            average1 /= counter;
            average2 /= size - counter;
            averagesq1 /= counter;
            averagesq2 /= size - counter;
            tmp = Math.sqrt(averagesq1 - average1 * average1)
                    + Math.sqrt(averagesq2 - average2 * average2);
            results[k] = Double.isNaN(tmp) ? 1e32 : tmp;
        }
        //Looking for the index corresponding to the minimal relative error
        int minIndex = IntStream.range(0, NUMPOINTS - 1).boxed()
                .min(comparingDouble(s -> results[s])).get();
        return delta * minIndex;
    }

    /**
     * Saving the image into a file
     *
     */
    private int saveImageFile(BufferedImage image, double[][] ar) {
        int res = 1;
        JFileChooser fo = new JFileChooser(imageWFile);
        fo.setDialogTitle(bundle.getString("IMAGE SAVE DIALOG TITLE"));
        for (FileFilter filter : imagefilters) {
            fo.addChoosableFileFilter(filter);
        }
        fo.setAcceptAllFileFilterUsed(false);
        int ans = fo.showSaveDialog(this);

        if (ans == JFileChooser.APPROVE_OPTION) {
            res = 0;
            try {
                imageWFile = fo.getSelectedFile();
                String type = ((FileNameExtensionFilter) fo.getFileFilter()).getExtensions()[0];
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
                res = 1;
                JOptionPane.showMessageDialog(null,
                        bundle.getString("IO SAVE ERROR DIALOG TITLE"),
                        bundle.getString("IO ERROR DIALOG"), JOptionPane.ERROR_MESSAGE);
            }
        }
        return res;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonImage;
    private javax.swing.JButton jButtonSegment;
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
    private javax.swing.JMenuItem jMenuItemLoadImageText;
    private javax.swing.JMenuItem jMenuItemLoadSegment;
    private javax.swing.JMenuItem jMenuItemSaveImage;
    private javax.swing.JMenuItem jMenuItemSaveImageText;
    private javax.swing.JMenuItem jMenuItemSaveVideo;
    private javax.swing.JMenuItem jMenuItemSegment;
    private javax.swing.JMenu jMenuLookAndFeel;
    private javax.swing.JMenu jMenuOptions;
    private javax.swing.JMenu jMenuTools;
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
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JSlider jSliderImages;
    private javax.swing.JTextField jTextFieldDiffCoef;
    private javax.swing.JTextField jTextFieldNSteps;
    private javax.swing.JTextField jTextFieldNonlinear;
    // End of variables declaration//GEN-END:variables
}
