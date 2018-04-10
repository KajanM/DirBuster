
/*
 * JPanelScanInfo.java
 *
 * Created on Nov 20, 2008, 8:38:45 PM
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.sittinglittleduck.DirBuster.gui;

import com.sittinglittleduck.DirBuster.Manager;
import com.sittinglittleduck.DirBuster.workGenerators.MultiThreadedGenerator;
import com.sittinglittleduck.DirBuster.workGenerators.WorkerGeneratorMultiThreaded;
import javax.swing.JOptionPane;

/**
 *
 * @author james
 */
public class JPanelScanInfo extends javax.swing.JPanel
{

    private int id;
    private Manager manager = Manager.getInstance();
    private MultiThreadedGenerator gen;
    private int type;

    /** Creates new form JPanelScanInfo */
    public JPanelScanInfo(int ThreadId, int type)
    {
        this.type = type;

        this.id = ThreadId;

        initComponents();
        /*
         * Adjust the display for the different types of generation.
         */
        if(type == Manager.LIST_BASED)
        {
            gen = manager.genThreads.elementAt(id);
            jProgressBar1.setMaximum((int) manager.getTotalPass());


            if(gen.getType() == WorkerGeneratorMultiThreaded.doDIR)
            {
                jLabelText.setText("Testing for dirs in " + gen.getStartpoint());
            }
            else if(gen.getType() == WorkerGeneratorMultiThreaded.doFile)
            {
                String ext = "extention " + gen.getFileExt();

                if(gen.getFileExt().equals(""))
                {
                    ext = "no extention";
                }
                jLabelText.setText("Testing for files in " + gen.getStartpoint() + " with " + ext);
            }
        }
        else if(type == Manager.BRUTE_BASED)
        {
            gen = manager.genThreads.elementAt(id);
            jProgressBar1.setMaximum((int) manager.getTotalPass());


            if(gen.getType() == WorkerGeneratorMultiThreaded.doDIR)
            {
                jLabelText.setText("Brute forcing dirs in " + gen.getStartpoint());
            }
            else if(gen.getType() == WorkerGeneratorMultiThreaded.doFile)
            {
                String ext = "extention " + gen.getFileExt();

                if(gen.getFileExt().equals(""))
                {
                    ext = "no extention";
                }
                jLabelText.setText("Brute forcing files in " + gen.getStartpoint() + " with " + ext);
            }
        }
        else if(type == Manager.LIST_BASED_FUZZ)
        {
            /*
             * this will be only one task, so dispable the buttons
             */
            jButtonStop.setEnabled(false);
            jToggleButtonPause.setEnabled(false);

            jLabelText.setText("List based fuzzing: " + manager.workGenFuzz.getStartPoint());

        }
        else if(type == Manager.BRUTE_BASED_FUZZ)
        {
            /*
             * this will be only one task, so dispable the buttons
             */
            jButtonStop.setEnabled(false);
            jToggleButtonPause.setEnabled(false);

            jLabelText.setText("Brute force fuzzing: " + manager.workGenBruteFuzz.getStartPoint());

        }
    }

    public void setProgressMax(int max)
    {
        jProgressBar1.setMaximum(max);
    }

    public void setProgressText(String text)
    {
        jProgressBar1.setString(text);
    }

    public void setProgressCurrentValue(int value)
    {

        jProgressBar1.setMaximum((int) manager.getTotalPass());
        
        boolean working = true;
        boolean paused = false;
        
        if(type == Manager.LIST_BASED)
        {
            working = gen.isWorking();
            paused = gen.isPaused();
        }
        else if(type == Manager.LIST_BASED_FUZZ)
        {
            working = manager.workGenFuzz.isWorking();
            paused = false;
            
        }
        else if(type == Manager.BRUTE_BASED)
        {
            
        }
        else if(type == Manager.BRUTE_BASED_FUZZ)
        {
            working = manager.workGenBruteFuzz.isWorking();
            paused = false;
        }

        if(!working)
        {
            jProgressBar1.setMaximum((int) manager.getTotalPass());
            jProgressBar1.setValue((int) manager.getTotalPass());
            jProgressBar1.setString("Complete");
            return;
        }
        if(value == -1)
        {
            jProgressBar1.setValue((int) manager.getTotalPass());
            jProgressBar1.setString("Complete");
        }
        else
        {

            jProgressBar1.setValue(value);
            if(paused)
            {
                jProgressBar1.setString("Paused");
            }
            else
            {
                int percent = (int) ((value / manager.getTotalPass()) * 100);
                jProgressBar1.setString(percent + "%");
            }

        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelText = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jToggleButtonPause = new javax.swing.JToggleButton();
        jButtonStop = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 1, 1));

        jLabelText.setText("jLabel1");

        jProgressBar1.setStringPainted(true);

        jToggleButtonPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/media-playback-pause.png"))); // NOI18N
        jToggleButtonPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonPauseActionPerformed(evt);
            }
        });

        jButtonStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sittinglittleduck/DirBuster/gui/icons/media-playback-stop.png"))); // NOI18N
        jButtonStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelText, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                        .addGap(10, 10, 10)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22)
                        .addComponent(jToggleButtonPause, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jButtonStop, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelText)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButtonPause, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonStop, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButtonPauseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jToggleButtonPauseActionPerformed
    {//GEN-HEADEREND:event_jToggleButtonPauseActionPerformed
        if(jToggleButtonPause.isSelected())
        {
            manager.pauseWorkGen(id);
        }
        else
        {
            manager.unPauseWorkGen(id);
        }
}//GEN-LAST:event_jToggleButtonPauseActionPerformed

    private void jButtonStopActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonStopActionPerformed
    {//GEN-HEADEREND:event_jButtonStopActionPerformed

        int n = -1;
        if(gen.getType() == WorkerGeneratorMultiThreaded.doDIR)
        {

            n = JOptionPane.showConfirmDialog(
                    manager.gui,
                    "Are you sure you wish to stop scanning for dirs in:\n" + gen.getStartpoint(),
                    "Are you sure?",
                    JOptionPane.YES_NO_OPTION);
        //if the anwser is yes
        }
        else
        {
            n = JOptionPane.showConfirmDialog(
                    manager.gui,
                    "Are you sure you wish to stop scanning for files with extention " + gen.getFileExt() + " in:\n" + gen.getStartpoint(),
                    "Are you sure?",
                    JOptionPane.YES_NO_OPTION);
        }

        if(n == 0)
        {
            manager.genThreads.elementAt(id).stopMe();
        }
    }//GEN-LAST:event_jButtonStopActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonStop;
    private javax.swing.JLabel jLabelText;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToggleButton jToggleButtonPause;
    // End of variables declaration//GEN-END:variables
}
