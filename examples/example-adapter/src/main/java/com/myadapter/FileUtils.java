/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.myadapter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

import com.hp.hpl.loom.adapter.BaseAdapter;

/**
 * Utility to get the file root for given adapter.
 *
 */
public final class FileUtils {

    private FileUtils() {}

    /**
     * Returns the file root from the properties and defaults to . if nothing is set.
     *
     * @param adapter the baseAdapter
     * @return File root to load from
     */
    public static File getFileRoot(final BaseAdapter adapter) {
        Object fileRoot = adapter.getAdapterConfig().getPropertiesConfiguration().getProperty("fileRoot");
        File f = null;
        if (StringUtils.isEmpty(fileRoot)) {
            f = new File(".");
        } else {
            f = new File((String) fileRoot);
        }
        return f;
    }

    protected static void getDirs(final File f, final Collection<File> currentDirs) {
        File[] dirs = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return !f.isFile();
            }
        });

        Collections.addAll(currentDirs, dirs);
        for (File dir : dirs) {
            FileUtils.getDirs(dir, currentDirs);
        }
    }

    protected static List<File> getAllAncestors(File base, File file) {
        List<File> files = new ArrayList<>();
        if (file != null) {
            boolean stop = false;
            file = file.getParentFile();
            while (!stop) {
                if (file == null || !file.toPath().startsWith(base.toPath())) {
                    stop = true;
                } else {
                    files.add(file);
                    file = file.getParentFile();
                }
            }
        }
        return files;
    }
}
