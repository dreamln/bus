/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.metric.xdsi;

import org.aoju.bus.image.galaxy.data.Code;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public class ClassificationBuilder {

    private final ClassificationType result;

    public ClassificationBuilder(String id) {
        this.result = new ClassificationType();
        this.result.setId(id);
        this.result.setObjectType("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification");
    }

    public ClassificationType build() {
        return this.result;
    }

    public ClassificationBuilder classificationScheme(String value) {
        this.result.setClassificationScheme(value);
        return this;
    }

    public ClassificationBuilder classifiedObject(String value) {
        this.result.setClassifiedObject(value);
        return this;
    }

    public ClassificationBuilder classificationNode(String value) {
        this.result.setClassificationNode(value);
        return this;
    }

    public ClassificationBuilder nodeRepresentation(String value) {
        this.result.setNodeRepresentation(value);
        return this;
    }

    public ClassificationBuilder code(Code code) {
        this.result.setNodeRepresentation(code.getCodeValue());
        this.result.setName(InternationalStringBuilder.build(code.getCodeMeaning()));

        this.result.getSlot().add(new SlotBuilder("codingScheme").valueList(code.getCodingSchemeDesignator()).build());
        return this;
    }

}
