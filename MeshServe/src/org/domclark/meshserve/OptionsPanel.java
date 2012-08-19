/*
 *  Copyright © 2012 Dominic Clark (TheSuccessor)
 *
 *  This file is part of MeshServe.
 *
 *  MeshServe is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MeshServe is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MeshServe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.domclark.meshserve;

import javax.swing.JPanel;

public abstract class OptionsPanel extends JPanel {

	private static final long serialVersionUID = 531608L;

	public abstract void onShow();
	public abstract void saveOptions();
	public abstract String getTitle();

}
