/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.schedule;

/**
 *
 * @author Divine Threepwood
 */
public class SyncObject {

	private final String name;

	public SyncObject(final Class clazz) {
		this(clazz.getSimpleName() + SyncObject.class.getSimpleName());
	}

	public SyncObject(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + name + "]";
	}
}
