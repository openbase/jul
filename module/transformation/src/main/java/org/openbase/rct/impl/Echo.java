package org.openbase.rct.impl;

/*-
 * #%L
 * RCT
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.openbase.rct.Transform;
import org.openbase.rct.TransformReceiver;
import org.openbase.rct.TransformerFactory;

public class Echo {

		public static void main(String[] args) {

		if (args.length != 2) {
			System.err.println("Required 2 arguments!");
			System.exit(1);
		}
		try {
			TransformReceiver transformer = TransformerFactory.getInstance()
					.createTransformReceiver();

			Thread.sleep(1000);

			Transform t = transformer.lookupTransform(args[0], args[1],
					System.currentTimeMillis());

			System.out.println(t);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		System.out.println("exit...");
		System.exit(0);
	}
}
