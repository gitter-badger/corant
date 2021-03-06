/*
 * Copyright (c) 2013-2018, Bingo.Chen (finesoft@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.elasticsearch.index.analysis;

import java.io.IOException;
import java.util.Arrays;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.CodepointCountFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;

/**
 * corant-suites-elastic-ext
 *
 * Adjust the NGram algorithm to preserve slices when the number of English words is less than the
 * specified minimum
 *
 * @author bingo 下午11:19:27
 *
 */
public class XEnNGramTokenFilter extends TokenFilter {

  public static final int MAX_NGRAM_SIZE = 15;
  public static final int MIN_NGRAM_SIZE = 3;

  public static final boolean RETAIN_TOKEN = true;

  private char[] actualTermBuffer;
  private boolean retainToken = false;
  private boolean filledCurToken = false;

  private int curCodePointCount;
  private int curGramSize;
  private int curPos;
  private int curPosInc, curPosLen;
  private char[] curTermBuffer;
  private int curTermLength;
  private int minGram, maxGram;
  private int tokEnd;
  private int tokStart;
  private final int xMinGram, xMaxGram;

  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
  private final PositionIncrementAttribute posIncAtt;
  private final PositionLengthAttribute posLenAtt;
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

  /**
   * 构造
   *
   * @param input 词流
   * @param enMinGram 英文最小长度
   * @param enMaxGram 英文最大长度
   * @param retainToken 保留分词
   */
  protected XEnNGramTokenFilter(TokenStream input, int enMinGram, int enMaxGram,
      boolean retainToken) {
    super(new CodepointCountFilter(input, 1, Integer.MAX_VALUE));
    if (enMinGram < 1) {
      throw new IllegalArgumentException("minGram must be greater than zero");
    }
    if (enMinGram > enMaxGram) {
      throw new IllegalArgumentException("minGram must not be greater than maxGram");
    }
    this.retainToken = retainToken;
    xMinGram = enMinGram;
    xMaxGram = enMaxGram;
    minGram = enMinGram;
    maxGram = enMaxGram;
    posIncAtt = addAttribute(PositionIncrementAttribute.class);
    posLenAtt = addAttribute(PositionLengthAttribute.class);
  }

  @Override
  public boolean incrementToken() throws IOException {
    while (true) {
      if (curTermBuffer == null) {
        if (!input.incrementToken()) {
          return false;
        } else {
          curTermBuffer = termAtt.buffer().clone();
          curTermLength = termAtt.length();
          curCodePointCount = Character.codePointCount(termAtt, 0, curTermLength);
          curPos = 0;
          curPosInc = posIncAtt.getPositionIncrement();
          curPosLen = posLenAtt.getPositionLength();
          tokStart = offsetAtt.startOffset();
          tokEnd = offsetAtt.endOffset();
          actualTermBuffer = Arrays.copyOf(curTermBuffer, curTermLength);
          int actualLen = actualTermBuffer.length;
          minGram = xMinGram > actualLen ? actualLen : xMinGram;
          maxGram = xMaxGram;
          curGramSize = minGram;
        }
      }

      if (curGramSize > maxGram || curPos + curGramSize > curCodePointCount) {
        ++curPos;
        curGramSize = minGram;
      }

      if (curPos + curGramSize <= curCodePointCount) {
        clearAttributes();
        final int start = Character.offsetByCodePoints(curTermBuffer, 0, curTermLength, 0, curPos);
        final int end =
            Character.offsetByCodePoints(curTermBuffer, 0, curTermLength, start, curGramSize);
        if (start == 1 && !filledCurToken && actualTermBuffer.length > maxGram
            && actualTermBuffer != null && retainToken) {
          termAtt.copyBuffer(actualTermBuffer, 0, actualTermBuffer.length);
          posIncAtt.setPositionIncrement(curPosInc);
          posLenAtt.setPositionLength(curPosLen);
          offsetAtt.setOffset(tokStart, tokEnd);
          filledCurToken = true;
          // watch(0, curTermLength);
          return true;
        }
        termAtt.copyBuffer(curTermBuffer, start, end - start);
        posIncAtt.setPositionIncrement(curPosInc);
        curPosInc = 0;
        posLenAtt.setPositionLength(curPosLen);
        offsetAtt.setOffset(tokStart, tokEnd);
        // watch(start, end);
        curGramSize++;
        return true;
      } else {
        filledCurToken = false;
      }
      curTermBuffer = null;
    }
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    curTermBuffer = null;
    actualTermBuffer = null;
  }

  void watch(int s, int e) {
    System.out.println("CTB: " + new String(curTermBuffer) + "\tCTL: " + curTermLength + "\tACTB: "
        + new String(actualTermBuffer) + "\t G: " + minGram + " ~ " + maxGram + "\tCPC: "
        + curCodePointCount + "\tT:" + new String(Arrays.copyOfRange(curTermBuffer, s, e))
        + "\t CG: " + curGramSize + "\tS-E: " + s + " ~ " + e + "\t RT: " + retainToken);
  }

}
