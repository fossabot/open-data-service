/*  Open Data Service
    Copyright (C) 2013  Tsysin Konstantin, Reischl Patrick

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 */
package integration.org.jvalue.ods.translator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.jvalue.ods.data.DataSource;
import org.jvalue.ods.data.generic.GenericValue;
import org.jvalue.ods.translator.XmlTranslator;

/**
 * The Class XmlTranslatorTest.
 */
public class XmlTranslatorTest {

	/** The grabber. */
	private XmlTranslator translator;

	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		translator = new XmlTranslator();
		assertNotNull(translator);
	}

	/**
	 * Test Translate.
	 */
	@Test
	public void testTranslate() {
		GenericValue gv = translator.translate(new DataSource("/nbgcity.osm",
				null));
		assertNotNull(gv);
	}

	/**
	 * Test Translate not existing file.
	 */
	@Test
	public void testTranslateNotExistingFile() {
		GenericValue gv = translator.translate(new DataSource(
				"NotExistingFile", null));
		assertNull(gv);
	}

	/**
	 * Test Translate null source.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testTranslateNullSource() {
		translator.translate(null);
	}
}
