/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.hardware.mac;

import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.hardware.AbstractFirmware;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * Firmware data obtained from ioreg.
 *
 * @author Kimi Liu
 * @version 5.2.8
 * @since JDK 1.8+
 */
final class MacFirmware extends AbstractFirmware {

    private final Supplier<EfiStrings> efi = Memoizer.memoize(this::queryEfi);

    @Override
    public String getManufacturer() {
        return efi.get().manufacturer;
    }

    @Override
    public String getName() {
        return efi.get().name;
    }

    @Override
    public String getDescription() {
        return efi.get().description;
    }

    @Override
    public String getVersion() {
        return efi.get().version;
    }

    @Override
    public String getReleaseDate() {
        return efi.get().releaseDate;
    }

    private EfiStrings queryEfi() {
        String releaseDate = null;
        String manufacturer = null;
        String version = null;
        String name = null;
        String description = null;

        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            IOIterator iter = platformExpert.getChildIterator("IODeviceTree");
            if (iter != null) {
                IORegistryEntry entry = iter.next();
                while (entry != null) {
                    switch (entry.getName()) {
                        case "rom":
                            byte[] data = entry.getByteArrayProperty("vendor");
                            if (data != null) {
                                manufacturer = new String(data, StandardCharsets.UTF_8);
                            }
                            data = entry.getByteArrayProperty("version");
                            if (data != null) {
                                version = new String(data, StandardCharsets.UTF_8);
                            }
                            data = entry.getByteArrayProperty("release-date");
                            if (data != null) {
                                releaseDate = new String(data, StandardCharsets.UTF_8);
                            }
                            break;
                        case "chosen":
                            data = entry.getByteArrayProperty("booter-name");
                            if (data != null) {
                                name = new String(data, StandardCharsets.UTF_8);
                            }
                            break;
                        case "efi":
                            data = entry.getByteArrayProperty("firmware-abi");
                            if (data != null) {
                                description = new String(data, StandardCharsets.UTF_8);
                            }
                            break;
                        default:
                            break;
                    }
                    entry.release();
                    entry = iter.next();
                }
                iter.release();
            }
            platformExpert.release();
        }
        return new EfiStrings(releaseDate, manufacturer, version, name, description);
    }

    private static final class EfiStrings {
        private final String releaseDate;
        private final String manufacturer;
        private final String version;
        private final String name;
        private final String description;

        private EfiStrings(String releaseDate, String manufacturer, String version, String name, String description) {
            this.releaseDate = StringUtils.isBlank(releaseDate) ? Builder.UNKNOWN : releaseDate;
            this.manufacturer = StringUtils.isBlank(manufacturer) ? Builder.UNKNOWN : manufacturer;
            this.version = StringUtils.isBlank(version) ? Builder.UNKNOWN : version;
            this.name = StringUtils.isBlank(name) ? Builder.UNKNOWN : name;
            this.description = StringUtils.isBlank(description) ? Builder.UNKNOWN : description;
        }
    }
}
