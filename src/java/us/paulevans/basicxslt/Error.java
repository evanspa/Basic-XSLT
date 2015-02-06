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

/**
 * Models an error message including a line and column number
 * @author pevans
 *
 */
public class Error {
	
	private int column;
	private int line;
	private String message;
	
	/**
	 * Constructor
	 * @param aColumn
	 * @param aLine
	 * @param aMessage
	 */
	public Error(int aColumn, int aLine, String aMessage) {
		column = aColumn;
		line = aLine;
		message = aMessage;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public int getColumn() {
		return column;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public int getLine() {
		return line;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public String getMessage() {
		return message;
	}
}
