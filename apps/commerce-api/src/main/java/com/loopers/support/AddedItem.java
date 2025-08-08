package com.loopers.support;

/**
 * @param <CHILD_ID>  child id
 * @param <PARENT_ID> parent id
 */
public interface AddedItem<CHILD_ID, PARENT_ID> {

    CHILD_ID getId();

    PARENT_ID getParentId();

}
