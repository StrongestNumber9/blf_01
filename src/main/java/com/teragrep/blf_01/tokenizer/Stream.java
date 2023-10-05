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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

public class Stream implements Supplier<Byte> {

    final InputStream inputStream;

    private final byte[] buffer = new byte[256 * 1024];
    private int pointer = -1;
    private int bytesInBuffer = -1;
    private byte b;
    private int mark;

    Stream(InputStream inputStream) {
        this.inputStream=inputStream;
    }

    public Byte get() {
        return b;
    }

    public void mark() {
        mark = pointer;
    }

    public int getMark() {
        return mark;
    }

    public void reset() {
        pointer = mark;
    }

    public void skip(int amount) {
        if (amount < 1) {
            return;
        }
        pointer = pointer + amount;
    }

    boolean next() {
        if (pointer == bytesInBuffer) {
            int read;
            try {
                read = inputStream.read(buffer, 0 , buffer.length);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }

            if (read <= 0) {
                pointer = bytesInBuffer;
                return false;

            }

            bytesInBuffer = read;
            pointer = 0;
        }

        b = buffer[pointer++];
        return true;
    }
}