/*
 * Copyright 2022-2023 Douglas Hoard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devopology.test.engine.internal.util;

public class Padding {

    int maxLength;

    public Padding() {
        // DO NOTHING
    }

    public int getPadding() {
        return maxLength;
    }

    public int apply(long value) {
        int valueLength = String.valueOf(value).length();
        if (valueLength > maxLength) {
            maxLength = valueLength;
        }
        return maxLength;
    }

    public int reset() {
        maxLength = 0;
        return maxLength;
    }

    @Override
    public String toString() {
        return String.valueOf(maxLength);
    }

    public static int calculatePadding(int ... values) {
        int maxLength = 0;

        for (int value : values) {
            int valueLength = String.valueOf(value).length();
            if (valueLength > maxLength) {
                maxLength = valueLength;
            }
        }

        return maxLength;
    }
}
