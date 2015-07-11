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
package shadowfileconverter;

import static TextUtilities.MyTextUtilities.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.EOFException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFormattedTextField;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.FormSubmitEvent;

/*
 * The program converts binary Shadow ray files to text files and
 * texts ray files to binary Shadow files
 *
 * @author Ruslan Feshchenko
 * @version 2.0
 */
public class ShadowFileConverterJForme extends javax.swing.JFrame {

    private boolean direction;
    private int maxNrays, bCol = 1, eCol = ShadowFiles.MAX_NCOL;
    private File rFile = null, wFile = null;
    private SwingWorker<Integer, Void> worker;
    private boolean working = false;
    JFormattedTextField maxRayNumberBox, beginColumn, endColumn;

    /**
     * Creates new form ShadowFileConverterJForme
     */
    public ShadowFileConverterJForme() {
        this.maxNrays = 100000;
        this.direction = false;
        maxRayNumberBox = getIntegerFormattedTextField(100000, 1, 10000000);
        beginColumn = getIntegerFormattedTextField(1, 1, ShadowFiles.MAX_NCOL);
        endColumn = getIntegerFormattedTextField(18, 1, ShadowFiles.MAX_NCOL);
        initComponents();
        ButtonGroup LFGroup = new ButtonGroup();
        LFGroup.add(DefaultJRadioButtonMenuItem);
        LFGroup.add(SystemJRadioButtonMenuItem);
        LFGroup.add(NimbusJRadioButtonMenuItem);
        UIManager.addPropertyChangeListener(e -> SwingUtilities.updateComponentTreeUI(this));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        UpperjPanel = new javax.swing.JPanel();
        ActionSelectionjComboBox = new javax.swing.JComboBox();
        actionJButton = new javax.swing.JButton();
        ProgressbarJPanel = new javax.swing.JPanel();
        jProgressBar = new javax.swing.JProgressBar();
        jMenuBar = new javax.swing.JMenuBar();
        OptionsjMenu = new javax.swing.JMenu();
        ParametersJMenuItem = new javax.swing.JMenuItem();
        LookAndFeelJMenu = new javax.swing.JMenu();
        DefaultJRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        SystemJRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        NimbusJRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        ScriptJMenuItem = new javax.swing.JMenuItem();
        HelpjMenu = new javax.swing.JMenu();
        HelpJMenuItem = new javax.swing.JMenuItem();
        AboutjMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Shadow file conversion");
        setResizable(false);

        UpperjPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        ActionSelectionjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Shadow binary -> text", "Text -> Shadow binary" }));
        ActionSelectionjComboBox.setToolTipText("");
        ActionSelectionjComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ActionSelectionjComboBoxActionPerformed(evt);
            }
        });

        actionJButton.setText("Start");
        actionJButton.setToolTipText("");
        actionJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout UpperjPanelLayout = new javax.swing.GroupLayout(UpperjPanel);
        UpperjPanel.setLayout(UpperjPanelLayout);
        UpperjPanelLayout.setHorizontalGroup(
            UpperjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UpperjPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ActionSelectionjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(actionJButton)
                .addContainerGap())
        );
        UpperjPanelLayout.setVerticalGroup(
            UpperjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UpperjPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(UpperjPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ActionSelectionjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(actionJButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        ProgressbarJPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Conversion progress", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        javax.swing.GroupLayout ProgressbarJPanelLayout = new javax.swing.GroupLayout(ProgressbarJPanel);
        ProgressbarJPanel.setLayout(ProgressbarJPanelLayout);
        ProgressbarJPanelLayout.setHorizontalGroup(
            ProgressbarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ProgressbarJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        ProgressbarJPanelLayout.setVerticalGroup(
            ProgressbarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ProgressbarJPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        OptionsjMenu.setText("Options");
        OptionsjMenu.setToolTipText("");

        ParametersJMenuItem.setText("Parameters...");
        ParametersJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ParametersJMenuItemActionPerformed(evt);
            }
        });
        OptionsjMenu.add(ParametersJMenuItem);

        LookAndFeelJMenu.setText("Look&Feel");

        DefaultJRadioButtonMenuItem.setSelected(true);
        DefaultJRadioButtonMenuItem.setText("Default");
        DefaultJRadioButtonMenuItem.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DefaultJRadioButtonMenuItemItemStateChanged(evt);
            }
        });
        LookAndFeelJMenu.add(DefaultJRadioButtonMenuItem);

        SystemJRadioButtonMenuItem.setText("System");
        SystemJRadioButtonMenuItem.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                SystemJRadioButtonMenuItemItemStateChanged(evt);
            }
        });
        LookAndFeelJMenu.add(SystemJRadioButtonMenuItem);

        NimbusJRadioButtonMenuItem.setText("Nimbus");
        NimbusJRadioButtonMenuItem.setToolTipText("");
        NimbusJRadioButtonMenuItem.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                NimbusJRadioButtonMenuItemItemStateChanged(evt);
            }
        });
        LookAndFeelJMenu.add(NimbusJRadioButtonMenuItem);

        OptionsjMenu.add(LookAndFeelJMenu);
        OptionsjMenu.add(jSeparator1);

        ScriptJMenuItem.setText("Script...");
        ScriptJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ScriptJMenuItemActionPerformed(evt);
            }
        });
        OptionsjMenu.add(ScriptJMenuItem);

        jMenuBar.add(OptionsjMenu);

        HelpjMenu.setText("Help");

        HelpJMenuItem.setText("Help topics...");
        HelpJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpJMenuItemActionPerformed(evt);
            }
        });
        HelpjMenu.add(HelpJMenuItem);

        AboutjMenuItem.setText("About...");
        AboutjMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AboutjMenuItemActionPerformed(evt);
            }
        });
        HelpjMenu.add(AboutjMenuItem);

        jMenuBar.add(HelpjMenu);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(UpperjPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(ProgressbarJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(UpperjPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ProgressbarJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ActionSelectionjComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ActionSelectionjComboBoxActionPerformed
        // TODO add your handling code here:
        direction = (ActionSelectionjComboBox.getSelectedIndex() == 1);
    }//GEN-LAST:event_ActionSelectionjComboBoxActionPerformed

    /**
     * Main action
     */
    private void actionJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionJButtonActionPerformed
        // TODO add your handling code here:
        if (working) {
            //Cancel Swingworker
            worker.cancel(true);
            return;
        }
        working = true;
        jProgressBar.setValue(0);
        jProgressBar.setStringPainted(true);
        actionJButton.setText("Stop");
        ((TitledBorder) ProgressbarJPanel.getBorder()).setTitle("Conversion progress");
        ProgressbarJPanel.repaint();
        /*
         * Create a new instance of Swingworker
         */
        worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                Integer processedRays = 0;
                /*
                 * Open source and sink files and doing conversion
                 */
                try (ShadowFiles shadowFileRead = new ShadowFiles(false, !direction, ShadowFiles.MAX_NCOL, maxNrays, rFile);
                        ShadowFiles shadowFileWrite = new ShadowFiles(true, direction, shadowFileRead.getNcol(), shadowFileRead.getNrays(), wFile)) {
                    int nrays = shadowFileRead.getNrays();
                    int ncols = shadowFileRead.getNcol();
                    double[] ray = new double[ncols];
                    int minCol = (bCol > ncols) ? ncols : bCol;
                    int maxCol = (eCol > ncols) ? ncols : eCol;
                    double[] truncray = new double[maxCol - minCol + 1];
                    rFile = shadowFileRead.getFile();
                    wFile = shadowFileWrite.getFile();
                    for (int i = 0; i < nrays; i++) {
                        // If canceled, return
                        if (isCancelled()) {
                            return processedRays;
                        }
                        shadowFileRead.read(ray);
                        if (!direction) {
                            for (int k = 0; k < maxCol - minCol + 1; k++) {
                                truncray[k] = ray[minCol + k];
                            }
                            shadowFileWrite.write(truncray);
                        } else {
                            shadowFileWrite.write(ray);
                        }
                        //Update progress bar
                        setProgressBar((int) (100 * (i + 1) / nrays));
                        processedRays++;
                    }
                    /*
                     * Processing various exception
                     */
                } catch (EOFException e) {
                    ShadowFiles.safeInvokeAndWait(() -> JOptionPane.showMessageDialog(null, "The end of file has been reached!", "Error",
                            JOptionPane.ERROR_MESSAGE));
                } catch (IOException e) {
                    ShadowFiles.safeInvokeAndWait(() -> JOptionPane.showMessageDialog(null, "I/O error during file conversion!", "Error",
                            JOptionPane.ERROR_MESSAGE));
                } catch (ShadowFiles.EndOfLineException e) {
                    ShadowFiles.safeInvokeAndWait(() -> JOptionPane.showMessageDialog(null, "The number of columns is less than specified on line "
                            + e.rayNumber + " !", "Error", JOptionPane.ERROR_MESSAGE));
                } catch (ShadowFiles.FileIsCorruptedException e) {
                    ShadowFiles.safeInvokeAndWait(() -> JOptionPane.showMessageDialog(null, "The file is corrupted! (line: "
                            + e.rayNumber + ")", "Error", JOptionPane.ERROR_MESSAGE));
                } catch (ShadowFiles.FileNotOpenedException e) {

                }
                return processedRays;
            }

            @Override
            protected void done() {
                Integer nProcessedrays = null;
                //Getting the number of rays processed
                try {
                    nProcessedrays = get();
                } catch (InterruptedException | CancellationException ex) {

                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof InvocationTargetException) {

                    } else {

                    }
                }
                working = false;
                actionJButton.setText("Start");
                //Reporting the number of rays processed or if it was interrupted
                TitledBorder border = (TitledBorder) ProgressbarJPanel.getBorder();
                if (nProcessedrays != null) {
                    border.setTitle("Number of rays processed: " + nProcessedrays.toString());
                } else {
                    border.setTitle("Processing interrupted!");
                }
                ProgressbarJPanel.repaint();
            }

            protected void setProgressBar(final int status) {
                javax.swing.SwingUtilities.invokeLater(() -> jProgressBar.setValue(status));
            }
        };
        worker.execute();
    }//GEN-LAST:event_actionJButtonActionPerformed

    private void AboutjMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AboutjMenuItemActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(null,
                "<html>Shadow file convertion. <br>Version: 2.0 <br>Date: July 2015. <br>Author: Ruslan Feshchenko</html>",
                "About ShadowFileConverter", 1);
    }//GEN-LAST:event_AboutjMenuItemActionPerformed

    /**
     * Changing the maximal number of rays
     */
    private void ParametersJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ParametersJMenuItemActionPerformed
        // TODO add your handling code here:
        //Creating JPanels for beginning and ending column numbers
        JPanel outerpanel = new JPanel();
        outerpanel.setBorder(BorderFactory.createTitledBorder(null,
                "Exported columns", TitledBorder.LEFT, TitledBorder.ABOVE_TOP));
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("First column:"));
        panel.add(beginColumn);
        panel.add(new JLabel("Last column:"));
        panel.add(endColumn);
        outerpanel.add(panel);
        beginColumn.setEnabled(!direction);
        endColumn.setEnabled(!direction);

        //Showing JOptionPane
        Object[] message = {
            "Maximal number of rays:", maxRayNumberBox,
            outerpanel
        };
        int option = JOptionPane.showConfirmDialog(null, message, "ShadowFileConverter parameters",
                JOptionPane.OK_CANCEL_OPTION);
        //Reading values and checking if eCol >= bCol
        if (option == JOptionPane.OK_OPTION) {
            maxNrays = (Integer) maxRayNumberBox.getValue();
            bCol = (Integer) beginColumn.getValue();
            eCol = (Integer) endColumn.getValue();
            eCol = (eCol < bCol) ? bCol : eCol;
            endColumn.setValue(eCol);
        }
    }//GEN-LAST:event_ParametersJMenuItemActionPerformed

    private void ScriptJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ScriptJMenuItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ScriptJMenuItemActionPerformed
    /*
     * Displaying help from a html resource file
     */
    private void HelpJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HelpJMenuItemActionPerformed
        // Creating JEditorPane to display the help file
        JEditorPane textArea = new JEditorPane();
        textArea.setPreferredSize(new Dimension(600, 400));
        textArea.setEditable(false);
        int PART_NUMBER = 5;
        int[] currentPart = {0};
        String[] strParts = new String[PART_NUMBER];
        String lastTag = "</div>";
        String firstTagBegin = "<div id=\"i";
        String firstTagEnd = "\">";

        //Registering PropertyChangeListerner to perform some actions when a document has loaded
        textArea.addPropertyChangeListener(pevt -> {
            if (pevt.getPropertyName().equals("page")) {
                //Getting the HTML document 
                HTMLDocument doc = (HTMLDocument) textArea.getDocument();
                try {
                    //Hiding all changeble parts except the first part
                    doc.setInnerHTML(doc.getElement("i0").getParentElement(), strParts[currentPart[0]]);
                } catch (BadLocationException | IOException ex) {
                    Logger.getLogger(ShadowFileConverterJForme.class.getName()).log(Level.SEVERE, null, ex);
                }
                //Disabling automatic handling of form submission events
                ((HTMLEditorKit) textArea.getEditorKit()).setAutoFormSubmission(false);
            }
        });

        //Registering a new hypertext link listerner for form submit events
        textArea.addHyperlinkListener(hevt -> {
            if (hevt instanceof javax.swing.text.html.FormSubmitEvent) {
                //Determining which button was pressed and incrementing or decrementing the index
                int prevInd = currentPart[0];
                currentPart[0] = ((FormSubmitEvent) hevt).getData().equals("back=Back") ? 
                        (--currentPart[0] + PART_NUMBER) % PART_NUMBER : ++currentPart[0] % PART_NUMBER;
                HTMLDocument doc = (HTMLDocument) textArea.getDocument();
                //Changing content based on the updated index
                try {
                    doc.setInnerHTML(doc.getElement("i" + prevInd).getParentElement(), strParts[currentPart[0]]);
                } catch (BadLocationException | IOException ex) {
                    Logger.getLogger(ShadowFileConverterJForme.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        //Reading the HTML help file
        try {
            textArea.setPage(ShadowFileConverterJForme.class.
                    getResource("/shadowfileconverterhelp/shadowfileconverterhelp.html"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error in the help file!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Reading the help file into a stringbuilder
        StringBuilder builder = new StringBuilder();
        try (BufferedReader stream = new BufferedReader(new InputStreamReader(ShadowFileConverterJForme.class.
                getResourceAsStream("/shadowfileconverterhelp/shadowfileconverterhelp.html")))) {
            for (String line = stream.readLine(); line != null; line = stream.readLine()) {
                builder.append(line).append("\n");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error in the help file!", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        //Separating strings with changeble parts of the HTML
        for (int i = 0; i < PART_NUMBER - 1; i++) {
            int ind = builder.indexOf(firstTagBegin + i + firstTagEnd);
            strParts[i] = builder.substring(ind, builder.indexOf(lastTag, ind) + 6);
        }
        strParts[PART_NUMBER - 1] = builder.substring(builder.indexOf(firstTagBegin + (PART_NUMBER - 1) + firstTagEnd),
                builder.indexOf(lastTag, builder.indexOf(firstTagBegin + (PART_NUMBER - 1) + firstTagEnd)) + 6);

        //Creating a scroll pane and label
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().add(textArea, BorderLayout.CENTER);
        Object[] message = {new JLabel("Program description",  SwingConstants.CENTER), scrollPane};
        //Showing help
        JOptionPane.showMessageDialog(null, message, "Help", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_HelpJMenuItemActionPerformed

    private void DefaultJRadioButtonMenuItemItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DefaultJRadioButtonMenuItemItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Logger.getLogger(ShadowFileConverterJForme.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_DefaultJRadioButtonMenuItemItemStateChanged

    private void SystemJRadioButtonMenuItemItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_SystemJRadioButtonMenuItemItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Logger.getLogger(ShadowFileConverterJForme.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_SystemJRadioButtonMenuItemItemStateChanged

    private void NimbusJRadioButtonMenuItemItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_NimbusJRadioButtonMenuItemItemStateChanged
        // TODO add your handling code here:
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                Logger.getLogger(ShadowFileConverterJForme.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_NimbusJRadioButtonMenuItemItemStateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        /*try {
         for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
         if ("Nimbus".equals(info.getName())) {
         javax.swing.UIManager.setLookAndFeel(info.getClassName());
         break;
         }
         }
         } catch (ClassNotFoundException ex) {
         java.util.logging.Logger.getLogger(ShadowFileConverterJForme.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
         java.util.logging.Logger.getLogger(ShadowFileConverterJForme.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
         java.util.logging.Logger.getLogger(ShadowFileConverterJForme.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
         java.util.logging.Logger.getLogger(ShadowFileConverterJForme.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }*/
        //</editor-fold>
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {

        }
        Locale.setDefault(new Locale("en", "US"));
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ShadowFileConverterJForme().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem AboutjMenuItem;
    private javax.swing.JComboBox ActionSelectionjComboBox;
    private javax.swing.JRadioButtonMenuItem DefaultJRadioButtonMenuItem;
    private javax.swing.JMenuItem HelpJMenuItem;
    private javax.swing.JMenu HelpjMenu;
    private javax.swing.JMenu LookAndFeelJMenu;
    private javax.swing.JRadioButtonMenuItem NimbusJRadioButtonMenuItem;
    private javax.swing.JMenu OptionsjMenu;
    private javax.swing.JMenuItem ParametersJMenuItem;
    private javax.swing.JPanel ProgressbarJPanel;
    private javax.swing.JMenuItem ScriptJMenuItem;
    private javax.swing.JRadioButtonMenuItem SystemJRadioButtonMenuItem;
    private javax.swing.JPanel UpperjPanel;
    private javax.swing.JButton actionJButton;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
