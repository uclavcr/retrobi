/*
 * Copyright 2012 UCL AV CR v.v.i.
 *
 * This file is part of Retrobi.
 *
 * Retrobi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Retrobi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Retrobi. If not, see <http://www.gnu.org/licenses/>.
 */

package cz.insophy.retrobi.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.insophy.retrobi.utils.type.CardRangeType;

/**
 * Card range for browsing a catalog. The class is immutable.
 * 
 * @author Vojtěch Hordějčuk
 */
public class CardRange implements Serializable {
    /**
     * default serial version
     */
    private static final long serialVersionUID = 1L;
    /**
     * total number of results
     */
    private final int count;
    /**
     * start offset, always >= 0
     */
    private final int offset;
    /**
     * range is a number of cards displayed
     */
    private final int range;
    /**
     * big step size (influences the "zoom" level, usually 10)
     */
    private final int bigstep;
    /**
     * this range is flat - that means, the highest range is BIGSTEP
     */
    private final boolean flat;
    /**
     * range type
     */
    private final CardRangeType type;
    
    /**
     * Creates a new instance.
     * 
     * @param startOffset
     * start offset
     * @param count
     * total card count
     * @param range
     * number of cards in the interval (<code>null</code> value means automatic)
     * @param bigstep
     * big step size (number of cards displayed / number of representants)
     */
    public CardRange(final int startOffset, final int count, final int range, final int bigstep) {
        this(startOffset, count, range, bigstep, false);
    }
    
    /**
     * Creates a new instance with the highest range possible.
     * 
     * @param startOffset
     * start offset
     * @param count
     * total card count
     * @param bigstep
     * big step size (number of cards displayed / number of representants)
     */
    public CardRange(final int startOffset, final int count, final int bigstep) {
        this(startOffset, count, bigstep, false);
    }
    
    /**
     * Creates a new instance with the highest range possible.
     * 
     * @param startOffset
     * start offset
     * @param count
     * total card count
     * @param bigstep
     * big step size (number of cards displayed / number of representants)
     * @param flat
     * enable flat mode
     */
    public CardRange(final int startOffset, final int count, final int bigstep, final boolean flat) {
        this(startOffset, count, CardRange.getHighestRange(count, bigstep, flat), bigstep, flat);
    }
    
    /**
     * Creates a new FLAT instance.
     * 
     * @param startOffset
     * start offset
     * @param count
     * total card count
     * @param range
     * number of cards in the interval (<code>null</code> value means automatic)
     * @param bigstep
     * big step size (number of cards displayed / number of representants)
     * @param flat
     * enable flat mode
     */
    private CardRange(final int startOffset, final int count, final int range, final int bigstep, final boolean flat) {
        this(count, startOffset, range, bigstep, CardRangeType.NONE, flat);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param count
     * total card count
     * @param startOffset
     * start offset
     * @param range
     * number of cards in the interval (<code>null</code> value means automatic)
     * @param bigstep
     * big step size (number of cards displayed / number of representants)
     * @param type
     * range type
     * @param flat
     * flat mode enabled
     */
    private CardRange(final int count, final int startOffset, final int range, final int bigstep, final CardRangeType type, final boolean flat) {
        if (count < 0) {
            throw new IllegalArgumentException(String.format("Počet musí být 0 a vyšší, byl %d.", count));
        }
        
        if ((startOffset < 0) || (range < 1)) {
            throw new IllegalArgumentException(String.format("Neplatné argumenty pro rozsah lístků (od %d, rozsah %d, krok %d).", startOffset, range, bigstep));
        }
        
        if (startOffset > Math.max(0, count - 1)) {
            throw new IllegalArgumentException(String.format("Počáteční offset %d nemůže být vyšší než %d - 1.", startOffset, count));
        }
        
        if (!(bigstep > 0)) {
            throw new IllegalArgumentException("Krok musí být větší než 0.");
        }
        
        if (type == null) {
            throw new NullPointerException();
        }
        
        this.count = count;
        this.offset = startOffset;
        this.bigstep = bigstep;
        this.type = type;
        this.flat = flat;
        this.range = range;
    }
    
    // =======
    // QUERIES
    // =======
    
    /**
     * Checks if the range contains more than one card.
     * 
     * @return <code>true</code> if the range contains more than one card,
     * <code>false</code> otherwise
     */
    public boolean hasMoreCards() {
        return (this.count > 1);
    }
    
    /**
     * Checks if the previous range exists.
     * 
     * @return <code>true</code> if exists, <code>false</code> otherwise
     */
    public boolean hasPrevious() {
        return (this.offset - this.range >= 0);
    }
    
    /**
     * Checks if the next range exists.
     * 
     * @return <code>true</code> if exists, <code>false</code> otherwise
     */
    public boolean hasNext() {
        return (this.offset + this.range < this.count);
    }
    
    /**
     * Checks if the upper range exists.
     * 
     * @return <code>true</code> if exists, <code>false</code> otherwise
     */
    public boolean hasUpper() {
        final int upperRange = this.range * this.bigstep;
        final int maxRange = CardRange.getHighestRange(this.getCount(), this.bigstep, this.flat);
        return (upperRange <= maxRange);
    }
    
    /**
     * Checks if the lower range exists.
     * 
     * @param addOffset
     * offset of the lower range added to the start offset
     * @return <code>true</code> if exists, <code>false</code> otherwise
     */
    public boolean hasLower(final int addOffset) {
        if (addOffset < 0) {
            return false;
        }
        
        if (this.range < this.bigstep) {
            return false;
        }
        
        final int lowerRange = this.range / this.bigstep;
        final int lowerOffset = this.offset + addOffset * lowerRange;
        
        if (lowerOffset <= this.getLastOffset()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if the range is on the first card.
     * 
     * @return <code>true</code> if range is on the first card,
     * <code>false</code> otherwise
     */
    public boolean isOnFirst() {
        return ((this.range == 1) && (this.offset == 0));
    }
    
    /**
     * Checks if the range is on the last card.
     * 
     * @return <code>true</code> if range is on the last card,
     * <code>false</code> otherwise
     */
    public boolean isOnLast() {
        return ((this.range == 1) && (this.offset == this.count - 1));
    }
    
    /**
     * Checks if the current range shows one card only.
     * 
     * @return <code>true</code> if only one card is shown by the current range,
     * <code>false</code> otherwise
     */
    public boolean isSingle() {
        return (this.range == 1);
    }
    
    // ========
    // CREATION
    // ========
    
    /**
     * Creates the same range but with different count. If the start offset is
     * higher than the new maximum, it will be lowered.
     * 
     * @param newCount
     * new count
     * @return a new range
     */
    public CardRange createForOtherCount(final int newCount) {
        return new CardRange(
                Math.max(0, Math.min(newCount - 1, this.offset)),
                newCount,
                this.range,
                this.bigstep,
                this.flat);
    }
    
    /**
     * Creates a lower range (if any).
     * 
     * @param addOffset
     * offset of the lower range added to the start offset
     * @return lower range
     */
    public CardRange createLower(final int addOffset) {
        if ((addOffset < 0) || (addOffset >= this.bigstep)) {
            throw new IllegalArgumentException(String.format(
                    "Nesprávný počet kroků (musí být 0 až %d, byl %d, rozsah: %s).",
                    this.bigstep - 1,
                    addOffset,
                    this.toString()));
        }
        
        if (!this.hasLower(addOffset)) {
            throw new UnsupportedOperationException("Nižší úroveň již neexistuje.");
        }
        
        final int newOffset = this.offset + addOffset * this.range / this.bigstep;
        final int newRange = this.range / this.bigstep;
        
        return new CardRange(
                this.count,
                newOffset,
                newRange,
                this.bigstep,
                CardRangeType.NONE,
                this.flat);
    }
    
    /**
     * Creates the previous range (if any).
     * 
     * @return previous range
     */
    public CardRange createPrevious() {
        if (!this.hasPrevious()) {
            throw new UnsupportedOperationException();
        }
        
        return new CardRange(
                this.count,
                this.offset - this.range,
                this.range,
                this.bigstep,
                CardRangeType.PREVIOUS,
                this.flat);
    }
    
    /**
     * Creates the next range (if any).
     * 
     * @return next range
     */
    public CardRange createNext() {
        if (!this.hasNext()) {
            throw new UnsupportedOperationException();
        }
        
        return new CardRange(
                this.count,
                this.offset + this.range,
                this.range,
                this.bigstep,
                CardRangeType.NEXT,
                this.flat);
    }
    
    /**
     * Creates the upper range (if any).
     * 
     * @return upper range
     */
    public CardRange createUpper() {
        if (!this.hasUpper()) {
            throw new UnsupportedOperationException();
        }
        
        return new CardRange(
                this.count,
                CardRange.roundOffset(this.offset, this.range * this.bigstep),
                this.range * this.bigstep,
                this.bigstep,
                CardRangeType.UPPER,
                this.flat);
    }
    
    /**
     * Creates the range showing the first card.
     * 
     * @return new range
     */
    public CardRange createFirst() {
        return new CardRange(
                this.count,
                0,
                1,
                this.bigstep,
                CardRangeType.FIRST,
                this.flat);
    }
    
    /**
     * Creates the range showing the last card.
     * 
     * @return new range
     */
    public CardRange createLast() {
        return new CardRange(
                this.count,
                this.count - 1,
                1,
                this.bigstep,
                CardRangeType.LAST,
                this.flat);
    }
    
    // =======
    // GETTERS
    // =======
    
    /**
     * Returns the first offset.
     * 
     * @return first offset
     */
    public int getFirstOffset() {
        return this.offset;
    }
    
    /**
     * Returns the last offset.
     * 
     * @return last offset
     */
    public int getLastOffset() {
        return Math.min(this.offset - 1 + this.range, this.count - 1);
    }
    
    /**
     * Returns the card count.
     * 
     * @return card count
     */
    public int getCount() {
        return this.count;
    }
    
    /**
     * Returns the limit.
     * 
     * @return limit (1 or BIGSTEP)
     */
    public int getLimit() {
        return (this.range == 1 ? 1 : this.bigstep);
    }
    
    /**
     * Returns the range.
     * 
     * @return range (1 or a multiply of BIGSTEP)
     */
    public int getRange() {
        return this.range;
    }
    
    /**
     * Returns the step size (cards to skip each step).
     * 
     * @return step size (1 or a multiply of BIGSTEP)
     */
    public int getStep() {
        if (this.range == 1) {
            return 0;
        }
        
        return this.range / this.bigstep;
    }
    
    /**
     * Checks if the step size equals 1. In other words, a continuous range
     * would be displayed.
     * 
     * @return <code>true</code> if the step size equals 1, <code>false</code>
     * otherwise
     */
    public boolean isSingleStep() {
        return (this.getStep() == 1) || (this.getLimit() == 1);
    }
    
    /**
     * Checks if the range is flat.
     * 
     * @return <code>true</code> if the range is flat, <code>false</code>
     * otherwise
     */
    public boolean isFlat() {
        return this.flat;
    }
    
    @Override
    public String toString() {
        return this.toString(this.type);
    }
    
    /**
     * Converts the range to a plain and simple string.
     * 
     * @return short string representation of the range object
     */
    public String toPlainString() {
        return this.toString(CardRangeType.NONE);
    }
    
    /**
     * Returns the string representation of the range.
     * 
     * @param tempType
     * final type to decide the string format
     * @return string representation of the range
     */
    private String toString(final CardRangeType tempType) {
        switch (tempType) {
            case FIRST:
                return CardRange.toRangeString(0, 0);
            case LAST:
                return CardRange.toRangeString(this.getCount() - 1, this.getCount() - 1);
            case PREVIOUS:
                return CardRange.toRangeString(this.getFirstOffset(), this.getLastOffset());
            case NEXT:
                return CardRange.toRangeString(this.getFirstOffset(), this.getLastOffset());
            case UPPER:
                return CardRange.toRangeString(this.getFirstOffset(), this.getLastOffset());
            default:
                return CardRange.toRangeString(this.getFirstOffset(), this.getLastOffset());
        }
    }
    
    /**
     * Helper method for range label generation.
     * 
     * @param start
     * start offset
     * @param end
     * end offset
     * @return label for the range
     */
    private static String toRangeString(final int start, final int end) {
        if (start == end) {
            return String.valueOf(start + 1);
        }
        
        return String.format("%d – %d", start + 1, end + 1);
    }
    
    // =====
    // USAGE
    // =====
    
    /**
     * Uses the card range to pick elements from the dynamic data loader.
     * 
     * @param source
     * the source data loader
     * @param <E>
     * element class
     * @return list of elements
     */
    public <E> List<E> useForPick(final DataLoader<E> source) {
        if (source == null) {
            // special case: NULL
            
            return null;
        }
        
        if ((this.getCount() <= 0) || (this.getLimit() <= 0)) {
            // special case: nothing to pick
            
            return Collections.emptyList();
        }
        
        if (this.getStep() == 1) {
            // continuous range
            
            final int limit = Math.min(this.getLimit(), this.getCount() - this.getFirstOffset());
            return CardRange.safeUserForPick(source, this.getFirstOffset(), limit);
        }
        
        // discrete range
        
        final List<E> result = new ArrayList<E>();
        
        int currentOffset = this.getFirstOffset();
        final int step = this.getStep();
        final int limit = this.getLimit();
        
        for (int i = 0; i < limit; i++) {
            if (currentOffset > this.count - 1) {
                // maximal index exceeded
                
                break;
            }
            
            result.addAll(CardRange.safeUserForPick(source, currentOffset, 1));
            currentOffset += step;
        }
        
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Uses the card range to pick elements from the list.
     * 
     * @param source
     * source list of elements
     * @param <E>
     * element class
     * @return list of elements picked
     */
    public <E> List<E> useForPick(final List<E> source) {
        return this.useForPick(new DataLoader<E>() {
            @Override
            public List<E> loadData(final int offset2, final int limit2) {
                if (source.isEmpty() || (offset2 >= source.size()) || (limit2 <= 0)) {
                    return Collections.emptyList();
                }
                
                final int to = Math.min(source.size(), offset2 + limit2);
                return source.subList(offset2, to);
            }
        });
    }
    
    /**
     * Helper method for safe offset/limit picking.
     * 
     * @param <E>
     * element class
     * @param source
     * source data loader
     * @param offset
     * offset
     * @param limit
     * limit
     * @return picked items or empty list
     */
    private static <E> List<E> safeUserForPick(final DataLoader<E> source, final int offset, final int limit) {
        CardRange.validateOffsetLimit(offset, limit);
        return source.loadData(offset, limit);
    }
    
    /**
     * Validates offset and limit.
     * 
     * @param offset
     * offset (number of items to skip, cannot be < 0)
     * @param limit
     * limit (item count limit, cannot be <= 0)
     */
    private static void validateOffsetLimit(final int offset, final int limit) {
        if ((offset < 0) || (limit <= 0)) {
            throw new IllegalArgumentException(String.format("Neplatný rozsah (offset = %d, limit = %d).", offset, limit));
        }
    }
    
    // ===============
    // UTILITY METHODS
    // ===============
    
    /**
     * Returns the highest range. Each range is either 1 or multiply of BIGSTEP.
     * The highest range is the highest multiply of BIGSTEP that is reasonable
     * for the given card count.
     * 
     * @param count
     * item count
     * @param bigstep
     * big step size
     * @param flat
     * flat mode enabled
     * @return highest range for the given parameters
     */
    private static int getHighestRange(final int count, final int bigstep, final boolean flat) {
        if (flat) {
            return bigstep;
        }
        
        if (count <= 1) {
            return 1;
        } else if (count <= bigstep) {
            return bigstep;
        } else if (count <= bigstep * bigstep) {
            return bigstep * bigstep;
        } else if (count <= bigstep * bigstep * bigstep) {
            return bigstep * bigstep * bigstep;
        } else if (count <= bigstep * bigstep * bigstep * bigstep) {
            return bigstep * bigstep * bigstep * bigstep;
        } else {
            // general case
            
            final int places = (int) (Math.log10(count - 1) / Math.log10(bigstep));
            final int power = (int) Math.floor(places);
            return bigstep * (int) Math.pow(bigstep, power);
        }
    }
    
    /**
     * Round offset using the provided step size. For example: If I can see card
     * 51, I am probably in range 51-60. The higher range is NOT 51-160, but
     * 1-100. This behavior is similar to reality.
     * 
     * @param offset
     * offset
     * @param step
     * step size
     * @return the same offset but rounded for the higher range
     */
    private static int roundOffset(final int offset, final int step) {
        return (offset / step) * step;
    }
}
