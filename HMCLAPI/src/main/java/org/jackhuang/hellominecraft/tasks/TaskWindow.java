/*
 * Copyright 2013 huangyuhui <huanghongxun2008@126.com>
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.
 */
package org.jackhuang.hellominecraft.tasks;

import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import org.jackhuang.hellominecraft.C;
import org.jackhuang.hellominecraft.HMCLog;
import org.jackhuang.hellominecraft.utils.system.MessageBox;
import org.jackhuang.hellominecraft.utils.StrUtils;
import org.jackhuang.hellominecraft.utils.SwingUtils;

/**
 *
 * @author huangyuhui
 */
public class TaskWindow extends javax.swing.JDialog
implements ProgressProviderListener, Runnable, DoingDoneListener<Task> {

    private static final TaskWindow instance = new TaskWindow();

    private static TaskWindow inst() {
        instance.clean();
        return instance;
    }

    public static TaskWindowFactory getInstance() {
        return new TaskWindowFactory();
    }

    boolean suc = false;

    private TaskList taskList;
    private final ArrayList<String> failReasons = new ArrayList();

    /**
     * Creates new form DownloadWindow
     */
    private TaskWindow() {
        initComponents();

        setLocationRelativeTo(null);

        if (lstDownload.getColumnModel().getColumnCount() > 1) {
            int i = 35;
            lstDownload.getColumnModel().getColumn(1).setMinWidth(i);
            lstDownload.getColumnModel().getColumn(1).setMaxWidth(i);
            lstDownload.getColumnModel().getColumn(1).setPreferredWidth(i);
        }

        setModal(true);
    }

    public TaskWindow addTask(Task task) {
        taskList.addTask(task);
        return this;
    }

    public synchronized void clean() {
        if (isVisible())
            return;
        taskList = new TaskList();
        taskList.addTaskListener(this);
        taskList.addAllDoneListener(this);
    }

    public boolean start() {
        if (isVisible() || taskList == null || taskList.isAlive())
            return false;
        pgsTotal.setValue(0);
        suc = false;
        SwingUtils.clearDefaultTable(lstDownload);
        failReasons.clear();
        tasks.clear();
        try {
            taskList.start();
        } catch (Exception e) {
            HMCLog.warn("Failed to start thread, maybe there're already a taskwindow here.", e);
            MessageBox.Show(C.i18n("taskwindow.no_more_instance"));
            return false;
        }
        this.setVisible(true);
        return this.areTasksFinished();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnCancel = new javax.swing.JButton();
        pgsTotal = new javax.swing.JProgressBar();
        srlDownload = new javax.swing.JScrollPane();
        lstDownload = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jackhuang/hellominecraft/launcher/I18N"); // NOI18N
        setTitle(bundle.getString("taskwindow.title")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        btnCancel.setText(bundle.getString("taskwindow.cancel")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        pgsTotal.setStringPainted(true);

        lstDownload.setModel(SwingUtils.makeDefaultTableModel(new String[]{C.i18n("taskwindow.file_name"), C.i18n("taskwindow.download_progress")}, new Class[]{String.class, String.class}, new boolean[]{false,false})
        );
        lstDownload.setRowSelectionAllowed(false);
        lstDownload.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        srlDownload.setViewportView(lstDownload);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pgsTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancel)
                .addContainerGap())
            .addComponent(srlDownload, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(srlDownload, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCancel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pgsTotal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        if (MessageBox.Show(C.i18n("operation.confirm_stop"), MessageBox.YES_NO_OPTION) == MessageBox.YES_OPTION)
            this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        if (taskList == null)
            return;
        tasks.clear();

        if (!this.failReasons.isEmpty()) {
            SwingUtilities.invokeLater(() -> MessageBox.Show(StrUtils.parseParams("", failReasons.toArray(), "\n"), C.i18n("message.error"), MessageBox.ERROR_MESSAGE));
            failReasons.clear();
        }

        if (!suc) {
            if (taskList != null)
                SwingUtilities.invokeLater(taskList::abort);
            HMCLog.log("Tasks have been canceled by user.");
        }
        taskList = null;
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JTable lstDownload;
    private javax.swing.JProgressBar pgsTotal;
    private javax.swing.JScrollPane srlDownload;
    // End of variables declaration//GEN-END:variables

    ArrayList<Task> tasks = new ArrayList<>();
    ArrayList<Integer> progresses = new ArrayList<>();

    @Override
    public void setProgress(Task task, int progress, int max) {
        SwingUtilities.invokeLater(() -> {
            int idx = tasks.indexOf(task);
            if (idx == -1)
                return;
            int pgs = progress * 100 / max;
            if (progresses.contains(idx) && progresses.get(idx) != pgs && lstDownload.getRowCount() > idx) {
                SwingUtils.setValueAt(lstDownload, pgs + "%", idx, 1);
                progresses.set(idx, pgs);
            }
        });
    }

    @Override
    public void run() {
        suc = true;
        this.dispose();
        HMCLog.log("Tasks are finished.");
    }

    @Override
    public void onDoing(Task task) {
        task.setProgressProviderListener(this);

        SwingUtilities.invokeLater(() -> {
            if (taskList == null)
                return;
            tasks.add(task);
            progresses.add(0);
            SwingUtils.appendLast(lstDownload, task.getInfo(), "0%");
            SwingUtils.moveEnd(srlDownload);
        });
    }

    public boolean areTasksFinished() {
        return suc;
    }

    @Override
    public void onDone(Task task) {
        SwingUtilities.invokeLater(() -> {
            if (taskList == null)
                return;
            pgsTotal.setMaximum(taskList.taskCount());
            pgsTotal.setValue(pgsTotal.getValue() + 1);
            int idx = tasks.indexOf(task);
            if (idx == -1)
                return;
            tasks.remove(idx);
            progresses.remove(idx);
            SwingUtils.removeRow(lstDownload, idx);
        });
    }

    @Override
    public void onFailed(Task task) {
        SwingUtilities.invokeLater(() -> {
            if (taskList == null)
                return;
            failReasons.add(task.getInfo() + ": " + (null == task.getFailReason() ? "No exception" : (StrUtils.isBlank(task.getFailReason().getLocalizedMessage()) ? task.getFailReason().getClass().getSimpleName() : task.getFailReason().getLocalizedMessage())));
            pgsTotal.setMaximum(taskList.taskCount());
            pgsTotal.setValue(pgsTotal.getValue() + 1);
            int idx = tasks.indexOf(task);
            if (idx == -1)
                return;
            SwingUtils.setValueAt(lstDownload, task.getFailReason(), idx, 0);
            SwingUtils.setValueAt(lstDownload, "0%", idx, 1);
            SwingUtils.moveEnd(srlDownload);
        });
    }

    @Override
    public void onProgressProviderDone(Task task) {

    }

    @Override
    public void setStatus(Task task, String sta) {
        SwingUtilities.invokeLater(() -> {
            if (taskList == null)
                return;
            int idx = tasks.indexOf(task);
            if (idx == -1)
                return;
            SwingUtils.setValueAt(lstDownload, task.getInfo() + ": " + sta, idx, 0);
        });
    }

    public static class TaskWindowFactory {

        LinkedList<Task> ll = new LinkedList<>();

        public TaskWindowFactory addTask(Task t) {
            ll.add(t);
            return this;
        }

        public boolean start() {
            synchronized (instance) {
                if (instance.isVisible())
                    return false;
                TaskWindow tw = inst();
                for (Task t : ll)
                    tw.addTask(t);
                return tw.start();
            }
        }
    }
}
