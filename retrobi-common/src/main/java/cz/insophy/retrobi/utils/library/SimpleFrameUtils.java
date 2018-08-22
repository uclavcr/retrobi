/*
 * Copyright 2012 UCL AV CR v.v.i.
 *
 * This file is part of Retrobi.
 *
 * Retrobi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Retrobi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Retrobi. If not, see <http://www.gnu.org/licenses/>.
 */

package cz.insophy.retrobi.utils.library;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Frame utility class.
 * 
 * @author Vojtěch Hordějčuk
 */
public final class SimpleFrameUtils {
    /**
     * Displays an error message dialog.
     * 
     * @param message
     * error message text
     */
    public static void showError(final String message) {
        JOptionPane.showMessageDialog(null, message, "Chyba", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Displays an error message dialog.
     * 
     * @param exception
     * exception thrown
     */
    public static void showError(final Exception exception) {
        JOptionPane.showMessageDialog(null, exception.getMessage(), "Chyba " + exception.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
        exception.printStackTrace();
    }
    
    /**
     * Displays an informational message dialog.
     * 
     * @param message
     * informational message text
     */
    public static void showInformation(final String message) {
        JOptionPane.showMessageDialog(null, message, "Informace", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Displays a confirmation dialog.
     * 
     * @param question
     * question to ask
     * @return user answer (yes = <code>true</code> no = <code>false</code>)
     */
    public static boolean showConfirm(final String question) {
        return (JOptionPane.showConfirmDialog(null, question, "Otázka", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
    }
    
    /**
     * Shows a directory chooser and sets the chosen file path to the target
     * text field. The chosen file is a directory and exists.
     * 
     * @param target
     * target text field
     * @return <code>true</code> if a valid directory was selected,
     * <code>false</code> otherwise
     */
    public static boolean chooseDirectory(final JTextField target) {
        // prepare chooser
        
        File defaultDir = new File(target.getText().trim());
        
        if (!defaultDir.exists() || !defaultDir.isDirectory()) {
            defaultDir = File.listRoots()[0];
        }
        
        final JFileChooser chooser = new JFileChooser(defaultDir);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        // open chooser and put its absolute path to the target component
        
        if (chooser.showOpenDialog(target) == JFileChooser.APPROVE_OPTION) {
            final File chosen = chooser.getSelectedFile();
            
            if (chosen != null) {
                target.setText(chosen.getAbsolutePath());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Cannot make instances of this class.
     */
    private SimpleFrameUtils() {
        throw new UnsupportedOperationException();
    }
}
