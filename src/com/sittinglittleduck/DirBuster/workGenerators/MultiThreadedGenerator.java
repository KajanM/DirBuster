/*
 * MultiThreadedGenerator.java
 *
 *
 * Copyright 2008 James Fisher
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

package com.sittinglittleduck.DirBuster.workGenerators;

/**
 *
 * @author James
 */
public abstract class MultiThreadedGenerator implements Runnable
{
    public abstract void stopMe();

    public abstract boolean isWorking();

    public abstract void pause();

    public abstract void unPause();

    public abstract boolean isPaused();

    public abstract int getType();

    public abstract String getStartpoint();

    public abstract int getCurrentPoint();

    public abstract String getFileExt();

}
