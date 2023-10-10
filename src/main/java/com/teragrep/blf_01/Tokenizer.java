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

package com.teragrep.blf_01;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to access the tokenizer
 */
public class Tokenizer {
    final Stream stream;
    final Entanglement entanglement;
    final TokenScan majorTokenScan;
    final TokenScan minorTokenScan;

    public Tokenizer() {

        final MajorDelimiters majorDelimiters = new MajorDelimiters();
        final MinorDelimiters minorDelimiters = new MinorDelimiters();

        this.stream = new Stream();
        this.entanglement = new Entanglement();
        this.majorTokenScan = new TokenScan(majorDelimiters);
        this.minorTokenScan = new TokenScan(minorDelimiters);

    }

    /**
     * Tokenizes a string.
     *
     * @param input String that is tokenized
     * @return String set of tokens from string
     */
    public List<String> tokenize(String input) {

        final ByteArrayInputStream bais = new ByteArrayInputStream(
                input.getBytes(StandardCharsets.US_ASCII)
        );

        stream.setInputStream(bais);

        return getTokensAsStringSet(stream);

    }

    /**
     * tokenizes an input stream.
     *
     * @param is input stream that is tokenized
     * @return String set of tokens from input stream
     */
    public List<String> tokenize(InputStream is) {

        stream.setInputStream(is);
        return getTokensAsStringSet(stream);

    }

    List<String> getTokensAsStringSet(Stream stream) {

        ArrayList<Token> majorTokens = majorTokenScan.findBy(stream);

        ArrayList<Token> allTokens = new ArrayList<>(majorTokens);

        for (Token token : majorTokens) {

            final ByteArrayInputStream tokenBais = new ByteArrayInputStream(token.bytes);

            stream.setInputStream(tokenBais);

            ArrayList<Token> minorTokens = minorTokenScan.findBy(stream);


            allTokens.addAll(entanglement.entangle(minorTokens));
        }

        return allTokens.stream().map(Token::toString).collect(Collectors.toList());

    }
}
