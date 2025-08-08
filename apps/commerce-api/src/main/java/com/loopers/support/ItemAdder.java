package com.loopers.support;

import com.loopers.support.error.BusinessException;
import com.loopers.support.error.CommonErrorType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemAdder {

    public static <PARENT_ID, CHILD extends AddedItem<CHILD_ID, PARENT_ID>, CHILD_ID> List<CHILD> addItemsTo(
            PARENT_ID parentId,
            List<CHILD> existingItems,
            List<CHILD> newItems,
            boolean childAlwaysHasParentId
    ) {
        if (CollectionUtils.isEmpty(newItems)) {
            return existingItems;
        }

        List<CHILD> those = new ArrayList<>(existingItems);
        Set<CHILD_ID> childIds = new HashSet<>();

        outer:
        for (CHILD child : newItems) {
            CHILD_ID childId = child.getId();
            if (childId != null && !childIds.add(childId)) {
                throw new BusinessException(CommonErrorType.CONFLICT);
            }

            if (childAlwaysHasParentId || child.getParentId() != null) {
                if (!Objects.equals(parentId, child.getParentId())) {
                    throw new BusinessException(CommonErrorType.INCONSISTENT);
                }
            }

            // 이미 추가된 아이템을 다시 추가하지 않는다.
            if (childId != null) {
                for (CHILD that : those) {
                    if (Objects.equals(childId, that.getId())) {
                        continue outer;
                    }
                }
            }

            those.add(child);
        }

        return List.copyOf(those);
    }

}
