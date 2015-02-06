/*
 Copyright 2006 Paul Evans 

 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 

 http://www.apache.org/licenses/LICENSE-2.0 

 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License.
 */
package us.paulevans.basicxslt;

import javax.swing.JFrame;

/**
 * Provides a dispose method that saves the frames size to the user's 
 * preferences object.
 * @author pevans
 *
 */
public class DisposableFrame extends JFrame {

    /**
     * Overridden dispose method.  Records the height and width of the frame
     * in the user's preferences before calling super.dispose().
     * @param aUserPrefs
     * @param aPropertyNamePrefix
     */
	public void dispose(UserPreferences aUserPrefs, 
			String aPropertyNamePrefix) {
		aUserPrefs.setProperty(aPropertyNamePrefix + 
				AppConstants.FRAME_HEIGHT_PROP, Integer.toString(getHeight()));
		aUserPrefs.setProperty(aPropertyNamePrefix + 
				AppConstants.FRAME_WIDTH_PROP, Integer.toString(getWidth()));
		super.dispose();
	} 
}
