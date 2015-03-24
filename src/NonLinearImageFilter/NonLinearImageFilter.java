/*
 * Non-linear image filtering
 */
package NonLinearImageFilter;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.FilteredImageSource;
import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.ComponentColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.BufferedImageOp;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

/**
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class NonLinearImageFilter extends javax.swing.JFrame {

    /**
     * Creates new form NonLinearImageFilter
     */
    private int xsize = 200;
    private int ysize = 200;
    private int noiseLevel=20;
    private double relativeSquareSize=0.5;
    private Vector<Image> imageList;
    private ColorModel grayColorModel;

    public NonLinearImageFilter() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        int[] nBits = {8};
        grayColorModel = new ComponentColorModel(cs, nBits, false, true, 
                Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        initComponents();
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
        jPanelResults = new javax.swing.JPanel();
        jPanelImages = new javax.swing.JPanel();
        jPanelControls = new javax.swing.JPanel();
        jScrollBarImages = new javax.swing.JScrollBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuOptions = new javax.swing.JMenu();
        jMenuHelp = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(100, 539));

        jPanelParam.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), "Parameters", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        jPanelParam.setMinimumSize(new java.awt.Dimension(100, 116));

        jLabelDiffCoef.setText("Diffusion coefficient");

        jTextFieldDiffCoef.setText("0.001");
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

        jTextFieldNonlinear.setText("0.1");
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

        jTextFieldNSteps.setText("100");
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
                .addGroup(jPanelParamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
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
                        .addComponent(jTextFieldNSteps)))
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

        jButtonImage.setText("Get image");
        jButtonImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonImageActionPerformed(evt);
            }
        });

        jButtonStart.setText("Start");
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

        javax.swing.GroupLayout jPanelSpaceLayout = new javax.swing.GroupLayout(jPanelSpace);
        jPanelSpace.setLayout(jPanelSpaceLayout);
        jPanelSpaceLayout.setHorizontalGroup(
            jPanelSpaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSpaceLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelSpaceLayout.setVerticalGroup(
            jPanelSpaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSpaceLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanelResults.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jPanelImages.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Images", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        javax.swing.GroupLayout jPanelImagesLayout = new javax.swing.GroupLayout(jPanelImages);
        jPanelImages.setLayout(jPanelImagesLayout);
        jPanelImagesLayout.setHorizontalGroup(
            jPanelImagesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 566, Short.MAX_VALUE)
        );
        jPanelImagesLayout.setVerticalGroup(
            jPanelImagesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanelControls.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Controls", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jScrollBarImages.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                jScrollBarImagesAdjustmentValueChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanelControlsLayout = new javax.swing.GroupLayout(jPanelControls);
        jPanelControls.setLayout(jPanelControlsLayout);
        jPanelControlsLayout.setHorizontalGroup(
            jPanelControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelControlsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollBarImages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(73, Short.MAX_VALUE))
        );
        jPanelControlsLayout.setVerticalGroup(
            jPanelControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelControlsLayout.createSequentialGroup()
                .addComponent(jScrollBarImages, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanelResultsLayout = new javax.swing.GroupLayout(jPanelResults);
        jPanelResults.setLayout(jPanelResultsLayout);
        jPanelResultsLayout.setHorizontalGroup(
            jPanelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelResultsLayout.createSequentialGroup()
                .addComponent(jPanelImages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelControls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 18, Short.MAX_VALUE))
        );
        jPanelResultsLayout.setVerticalGroup(
            jPanelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelResultsLayout.createSequentialGroup()
                .addGroup(jPanelResultsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelImages, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelControls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(jPanelResults, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jScrollPane1.setViewportView(jPanel2);

        jMenuFile.setText("File");
        jMenuBar1.add(jMenuFile);

        jMenuOptions.setText("Options");
        jMenuBar1.add(jMenuOptions);

        jMenuHelp.setText("Help");
        jMenuBar1.add(jMenuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 745, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldDiffCoefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDiffCoefActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldDiffCoefActionPerformed

    private void jButtonStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStartActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonStartActionPerformed

    private void jButtonImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonImageActionPerformed
        // TODO add your handling code here:
        
        Image inImage = jPanelImages.createImage(new MemoryImageSource(xsize, 
               ysize, grayColorModel, generatePixelData(xsize, ysize, 
                       relativeSquareSize, noiseLevel), 0, xsize));
        imageList = new Vector<>();
        imageList.add(inImage);
        
        JComponent Component = new ImageComponent(inImage, jPanelImages.getWidth(), jPanelImages.getHeight());
        jPanelImages.add(Component);
        jPanelImages.setLayout(new BorderLayout(10, 10));
        jPanelImages.add(Component, BorderLayout.CENTER);
        jPanelImages.revalidate();
        jPanelImages.repaint();
    }//GEN-LAST:event_jButtonImageActionPerformed

    private void jTextFieldNonlinearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldNonlinearActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldNonlinearActionPerformed

    private void jTextFieldNStepsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldNStepsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldNStepsActionPerformed

    private void jTextFieldDiffCoefFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldDiffCoefFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldDiffCoefFocusLost

    private void jTextFieldNonlinearFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldNonlinearFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldNonlinearFocusLost

    private void jTextFieldNStepsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldNStepsFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldNStepsFocusLost

    private void jScrollBarImagesAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_jScrollBarImagesAdjustmentValueChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jScrollBarImagesAdjustmentValueChanged

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
    
    /*
    * The method generates pixel arrays for test images
    */
    private byte[] generatePixelData(int xsize, int ysize, double squareScale, int noise) {
        byte[] pixels = new byte[xsize * ysize];
        for (int i = 0; i < xsize; i++) {
            for (int k = 0; k < xsize; k++) {
                if ((Math.abs(i - xsize / 2 + 1) < squareScale * ysize / 2) && (Math.abs(k - ysize / 2 + 1) < squareScale * xsize / 2)) {
                    pixels[i * xsize + k] = 0;
                } else {
                    pixels[i * xsize + k] = (byte) 127;
                }
                pixels[i * xsize + k] += (byte) (Math.random() * noise);
            }
        }
        return pixels;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonImage;
    private javax.swing.JButton jButtonStart;
    private javax.swing.JLabel jLabelDiffCoef;
    private javax.swing.JLabel jLabelNSteps;
    private javax.swing.JLabel jLabelNonlinear;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenu jMenuOptions;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelAction;
    private javax.swing.JPanel jPanelControls;
    private javax.swing.JPanel jPanelImages;
    private javax.swing.JPanel jPanelParam;
    private javax.swing.JPanel jPanelResults;
    private javax.swing.JPanel jPanelSpace;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JScrollBar jScrollBarImages;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldDiffCoef;
    private javax.swing.JTextField jTextFieldNSteps;
    private javax.swing.JTextField jTextFieldNonlinear;
    // End of variables declaration//GEN-END:variables
}
