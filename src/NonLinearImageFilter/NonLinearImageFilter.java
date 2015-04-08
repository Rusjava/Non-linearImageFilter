/*
 * Non-linear image filtering
 */
package NonLinearImageFilter;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingWorker;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.imageio.ImageIO;

import TextUtilities.MyTextUtilities;
import java.awt.color.ColorSpace;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

/**
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class NonLinearImageFilter extends javax.swing.JFrame {

    /**
     * Creates new form NonLinearImageFilter
     */
    private final ImageParam imageParam;
    private ArrayList<JComponent> imageList;
    private int nSteps = 10;
    private int sliderposition = 50;
    private double diffCoef = 0.01;
    private double nonLinearCoef = 1;
    private boolean testFlag = false;
    private boolean nonLinearFlag = false;
    private CrankNicholson2D comp;
    private final Map defaults;
    private boolean working = false;
    private SwingWorker<Void, Void> worker;
    private ArrayList<double[][]> dataList;
    private final JTextField xsizeField, ysizeField, noiseField, signalField, scaleField;

    public NonLinearImageFilter() {
        this.imageList = new ArrayList<>();
        this.defaults = new HashMap();
        this.imageParam = new ImageParam();
        this.xsizeField = new JTextField("300");
        this.ysizeField = new JTextField("200");
        this.noiseField = new JTextField("14");
        this.signalField = new JTextField("15");
        this.scaleField = new JTextField("0.5");

        initComponents();
        jButtonStart.setEnabled(false);
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
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemSaveVideo = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuOptions = new javax.swing.JMenu();
        jMenuItemImageOptions = new javax.swing.JMenuItem();
        jMenuItemFilterOptions = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemHelp = new javax.swing.JMenuItem();
        jMenuItemAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(100, 539));

        jPanelParam.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Parameters", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        jPanelParam.setMinimumSize(new java.awt.Dimension(100, 116));

        jLabelDiffCoef.setText("Diffusion coefficient");

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

        jLabelNonlinear.setText("Non-linear coefficient");

        jTextFieldNonlinear.setText("10000");
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

        jLabelNSteps.setText("Number of steps");

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

        jPanelAction.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Actions", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        jPanelAction.setMinimumSize(new java.awt.Dimension(100, 116));

        jButtonImage.setText("Initialize");
        jButtonImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonImageActionPerformed(evt);
            }
        });

        jButtonStart.setText("Filter");
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

        jPanelSpace.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Progress", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        jPanelSpace.setMinimumSize(new java.awt.Dimension(100, 116));

        jCheckBoxNonLinear.setText("Non-linear filtering");
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
                    .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanelSpaceLayout.createSequentialGroup()
                        .addComponent(jCheckBoxNonLinear, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
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

        jPanelImages.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Images", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        javax.swing.GroupLayout jPanelImagesLayout = new javax.swing.GroupLayout(jPanelImages);
        jPanelImages.setLayout(jPanelImagesLayout);
        jPanelImagesLayout.setHorizontalGroup(
            jPanelImagesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 594, Short.MAX_VALUE)
        );
        jPanelImagesLayout.setVerticalGroup(
            jPanelImagesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanelControls.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Controls", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jSliderImages.setOrientation(javax.swing.JSlider.VERTICAL);
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
                .addComponent(jPanelControls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14))
        );
        jPanelResultsLayout.setVerticalGroup(
            jPanelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelResultsLayout.createSequentialGroup()
                .addGroup(jPanelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelControls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelImages, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 22, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelResults, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(jPanelResults, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel2);

        jMenuFile.setText("File");

        jMenuItemSaveVideo.setText("Save as video...");
        jMenuItemSaveVideo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveVideoActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSaveVideo);
        jMenuFile.add(jSeparator1);

        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBar.add(jMenuFile);

        jMenuOptions.setText("Options");

        jMenuItemImageOptions.setText("Image options...");
        jMenuItemImageOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemImageOptionsActionPerformed(evt);
            }
        });
        jMenuOptions.add(jMenuItemImageOptions);

        jMenuItemFilterOptions.setText("Filter options...");
        jMenuItemFilterOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFilterOptionsActionPerformed(evt);
            }
        });
        jMenuOptions.add(jMenuItemFilterOptions);

        jMenuBar.add(jMenuOptions);

        jMenuHelp.setText("Help");

        jMenuItemHelp.setText("Display help...");
        jMenuItemHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHelpActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemHelp);

        jMenuItemAbout.setText("About...");
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
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 769, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 599, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldDiffCoefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDiffCoefActionPerformed
        // TODO add your handling code here:
        diffCoef = MyTextUtilities.TestValueWithMemory(0, 10, jTextFieldDiffCoef,
                "0.3", defaults);
    }//GEN-LAST:event_jTextFieldDiffCoefActionPerformed

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartActionPerformed
        // TODO add your handling code here:
        if (working == true) {
            worker.cancel(false);
            return;
        }
        jProgressBar.setValue(0);
        jProgressBar.setStringPainted(true);
        working = true;
        comp = new CrankNicholson2D(new double[]{-1, 0, 1}, diffCoef, nonLinearCoef);
        jButtonStart.setText("Stop");
        jButtonImage.setEnabled(false);
        worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                ImageParam imageParamClone = (ImageParam) imageParam.clone();
                double [][] currentData;
                JComponent component;
                for (int i = 0; i < nSteps; i++) {
                    if (isCancelled()) {
                        return null;
                    }
                    if (testFlag) {
                        imageParamClone.scale = imageParam.scale * (nSteps - i) / nSteps;
                        component = new ImageComponent(imageParamClone);
                        currentData = ((ImageComponent) component).getPixelData();
                    } else {
                        if (nonLinearFlag) {
                            currentData = comp.solveNonLinear(dataList.get(dataList.size() - 1));
                        } else {
                            currentData = comp.solveLinear(dataList.get(dataList.size() - 1));
                        }
                        component=new ImageComponent(currentData);
                    }
                    imageList.add(component);
                    dataList.add(currentData);
                    setStatusBar((int) (100.0 * (i + 1) / nSteps));
                }
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
                        JOptionPane.showMessageDialog(null, "Error!", "Error", JOptionPane.ERROR_MESSAGE);
                        Logger.getLogger(NonLinearImageFilter.class.getName()).log(Level.SEVERE, null, e);
                        return;
                    }
                }
                updateImagePanel((int) (sliderposition * (imageList.size() - 1) / 100.0));
                working = false;
                jButtonStart.setText("Filter");
                jButtonImage.setEnabled(true);
            }

            /**
             * Updating progress bar and displaying the last image
             *
             * @param status
             */
            public void setStatusBar(final int status) {
                SwingUtilities.invokeLater(() -> {
                    updateImagePanel(imageList.size() - 1);
                    jProgressBar.setValue(status);
                });
            }
        };
        worker.execute();
    }//GEN-LAST:event_jButtonStartActionPerformed

    private void jButtonImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImageActionPerformed
        // TODO add your handling code here
        JComponent component = null;
        /*
         * create a button group to chose the source of initial image
         */
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton button1 = new JRadioButton("Generate");
        button1.setSelected(true);
        JRadioButton button2 = new JRadioButton("From file");
        buttonGroup.add(button1);
        buttonGroup.add(button2);
        JPanel panel = new JPanel();
        panel.add(button1);
        panel.add(button2);
        /*
         * Display option window
         */
        Object[] message = {panel};
        int option = JOptionPane.showConfirmDialog(null, message, "Choose image source", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            /*
             * if OK, proceed to generate/load image
             */
            if (button2.isSelected()) {
                /*
                 * if the second choice, load image from file
                 */
                JFileChooser fo = new JFileChooser();
                fo.setDialogTitle("Choose grayscale image to load");
                int ans = fo.showOpenDialog(this);
                if (ans == JFileChooser.APPROVE_OPTION) {
                    try {
                        BufferedImage image = ImageIO.read(fo.getSelectedFile());
                        if (image.getColorModel().getColorSpace().getType()
                                == ColorSpace.TYPE_GRAY) {
                            component = new ImageComponent(image);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "<html>The image is not grayscale! Type: </html>",
                                    "Image Error!", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,
                                "<html>Error while reading the image file</html>",
                                "IO Error!", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    return;
                }
            } else {
                /*
                 * if the first choice, generate image
                 */
                component = new ImageComponent(imageParam);
            }
            imageList = new ArrayList<>();
            dataList = new ArrayList<>();
            imageList.add(component);
            dataList.add(((ImageComponent) component).getPixelData());
            updateImagePanel(0);
            jButtonStart.setEnabled(true);
        }
    }//GEN-LAST:event_jButtonImageActionPerformed

    private void jTextFieldNonlinearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldNonlinearActionPerformed
        // TODO add your handling code here:
        nonLinearCoef = MyTextUtilities.TestValueWithMemory(0, 1000000, jTextFieldNonlinear,
                "10000", defaults);
    }//GEN-LAST:event_jTextFieldNonlinearActionPerformed

    private void jTextFieldNStepsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldNStepsActionPerformed
        // TODO add your handling code here:
        nSteps = (int) Math.round(MyTextUtilities.TestValueWithMemory(0, 1000, jTextFieldNSteps,
                "10", defaults));
    }//GEN-LAST:event_jTextFieldNStepsActionPerformed

    private void jTextFieldDiffCoefFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDiffCoefFocusLost
        // TODO add your handling code here:
        diffCoef = MyTextUtilities.TestValueWithMemory(0, 10, jTextFieldDiffCoef,
                "0.3", defaults);
    }//GEN-LAST:event_jTextFieldDiffCoefFocusLost

    private void jTextFieldNonlinearFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldNonlinearFocusLost
        // TODO add your handling code here:
        nonLinearCoef = MyTextUtilities.TestValueWithMemory(0, 1000000, jTextFieldNonlinear,
                "10000", defaults);
    }//GEN-LAST:event_jTextFieldNonlinearFocusLost

    private void jTextFieldNStepsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldNStepsFocusLost
        // TODO add your handling code here:
        nSteps = (int) Math.round(MyTextUtilities.TestValueWithMemory(0, 1000, jTextFieldNSteps,
                "10", defaults));
    }//GEN-LAST:event_jTextFieldNStepsFocusLost

    private void jSliderImagesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderImagesStateChanged
        // TODO add your handling code here:
        JSlider source = (JSlider) evt.getSource();
        if (!source.getValueIsAdjusting()) {
            sliderposition = source.getValue();
            updateImagePanel((int) (sliderposition * (imageList.size() - 1) / 100.0));
        }
    }//GEN-LAST:event_jSliderImagesStateChanged

    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(null,
                "<html>Non-linear image filter. <br>Version: 0.1 <br>Date: April 2015. <br>Author: Ruslan Feshchenko</html>",
                "About NonLinear image filter", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItemAboutActionPerformed

    private void jMenuItemHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHelpActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItemHelpActionPerformed

    private void jMenuItemImageOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemImageOptionsActionPerformed
        // TODO add your handling code here:
        Object[] message = {
            "Image width, px:", xsizeField,
            "Image height, px:", ysizeField,
            "Log of noise amplitude:", noiseField,
            "Log of signal amplitude:", signalField,
            "Scale:", scaleField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Image generation parameters",
                JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            imageParam.xsize = (int) Math.round(MyTextUtilities.TestValueWithMemory(0,
                    1000, xsizeField, "300", defaults));
            imageParam.ysize = (int) Math.round(MyTextUtilities.TestValueWithMemory(0,
                    1000, ysizeField, "200", defaults));
            imageParam.noise = (int) Math.pow(2, Math.round(MyTextUtilities.TestValueWithMemory(0,
                    15, noiseField, "14", defaults)));
            imageParam.signal = (int) Math.pow(2, Math.round(MyTextUtilities.TestValueWithMemory(0,
                    16, signalField, "15", defaults)));
            imageParam.scale = MyTextUtilities.TestValueWithMemory(0,
                    1, scaleField, "0.5", defaults);
        }
    }//GEN-LAST:event_jMenuItemImageOptionsActionPerformed

    private void jMenuItemSaveVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveVideoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItemSaveVideoActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jMenuItemFilterOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFilterOptionsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItemFilterOptionsActionPerformed

    private void jCheckBoxNonLinearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNonLinearActionPerformed
        // TODO add your handling code here:
        nonLinearFlag=jCheckBoxNonLinear.isSelected();
    }//GEN-LAST:event_jCheckBoxNonLinearActionPerformed

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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NonLinearImageFilter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NonLinearImageFilter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NonLinearImageFilter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NonLinearImageFilter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NonLinearImageFilter().setVisible(true);
            }
        });
    }

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
        jPanelImages.revalidate();
        jPanelImages.repaint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonImage;
    private javax.swing.JButton jButtonStart;
    private javax.swing.JCheckBox jCheckBoxNonLinear;
    private javax.swing.JLabel jLabelDiffCoef;
    private javax.swing.JLabel jLabelNSteps;
    private javax.swing.JLabel jLabelNonlinear;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemFilterOptions;
    private javax.swing.JMenuItem jMenuItemHelp;
    private javax.swing.JMenuItem jMenuItemImageOptions;
    private javax.swing.JMenuItem jMenuItemSaveVideo;
    private javax.swing.JMenu jMenuOptions;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelAction;
    private javax.swing.JPanel jPanelControls;
    private javax.swing.JPanel jPanelImages;
    private javax.swing.JPanel jPanelParam;
    private javax.swing.JPanel jPanelResults;
    private javax.swing.JPanel jPanelSpace;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSlider jSliderImages;
    private javax.swing.JTextField jTextFieldDiffCoef;
    private javax.swing.JTextField jTextFieldNSteps;
    private javax.swing.JTextField jTextFieldNonlinear;
    // End of variables declaration//GEN-END:variables
}
