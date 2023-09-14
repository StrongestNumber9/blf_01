/*
 * Teragrep Bloom Filter Library BLF-01
 * Copyright (C) 2019, 2020, 2021, 2022, 2023 Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://github.com/teragrep/teragrep/blob/main/LICENSE>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */

package com.teragrep.blf_01.tokenizer;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class MajorTokenStream {
    private static final String regex = "(\\t|\\n|\\r| |\\!|\\\"|%0A|%20|%21|%2520|%2526|%26|%28|%29|%2B|%2C|%3A|%3B|%3D|%5B|%5D|%7C|&|\\'|\\(|\\)|\\*|\\+|,|--|;|<|>|\\?|\\[|\\]|\\{|\\||\\})";
    private static final Pattern compiledRegex = Pattern.compile(regex);
    private final String input;
    private final Set<String> tokens;

    public MajorTokenStream(String input) throws IOException {
        this.input = input;
        this.tokens = split();
    }

    public Set<String> getTokenSet() {
        return tokens;
    }

    private Set<String> split() throws IOException {

        final Set<String> resultSet = new HashSet<>();
        final StringReader reader = new StringReader(input);
        final StringBuilder builder = new StringBuilder();
        final StringBuilder overlap = new StringBuilder();

        while (true) {
            int ascii = reader.read();
            if (ascii < 0) {
                resultSet.add(builder.toString());
                break;
            }
            char ch = (char) ascii;
            builder.append(ch);

            // Handle multiple character regex
            if (ascii == 37 || ascii == 45) {
                reader.mark(5);
                overlap.append(ch);
                for (int i = 1; i <= 5; i++) {
                    char overlapChar = (char) reader.read();
                    overlap.append(overlapChar);

                    if (match(overlap.toString())) {
                        resultSet.add(overlap.toString());
                        resultSet.add(builder.deleteCharAt(builder.length() - 1).toString());
                        builder.setLength(0);
                        overlap.setLength(0);
                        reader.reset();
                        reader.skip(i);
                        break;
                    } else if (i == 5) {
                        overlap.setLength(0);
                        reader.reset();
                    }
                }
            }

            if (match(String.valueOf(ch))) {
                resultSet.add(builder.toString());
                builder.setLength(0);
            }
        }
        reader.close();

        return resultSet;
    }


    private boolean match(String value) {
        return value.matches(compiledRegex.pattern());
    }
}