/*
 * jMemorize - Learning made easy (and fun) - A Leitner flashcards tool
 * Copyright(C) 2004-2008 Riad Djemili and contributors
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package jmemorize.gui.swing.actions.file;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import jmemorize.core.Main;
import jmemorize.gui.Localization;
import jmemorize.gui.swing.actions.AbstractSessionDisabledAction;

/**
 * An action that opens up a file chooser and saves the lesson at that location.
 * 
 * @author djemili
 */
public class DropboxSyncAction extends AbstractSessionDisabledAction
{
    public DropboxSyncAction()
    {
        setValues();
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener
     */
    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        Main main = Main.getInstance();
        main.getFrame().openSyncDialog();
    }
    
    private void setValues()
    {
        setName(Localization.get("MainFrame.SYNC")); //$NON-NLS-1$
        setDescription(Localization.get("MainFrame.SYNC_DESC")); //$NON-NLS-1$
        setIcon("/resource/icons/Dropbox-icon.png"); //$NON-NLS-1$
        setAccelerator(KeyEvent.VK_Y, SHORTCUT_KEY + InputEvent.SHIFT_MASK);
        setMnemonic(2);
    }
}