package com.spaceflow.owner;

import com.spaceflow.room.Room;
import com.spaceflow.room.RoomRepository;
import com.spaceflow.room.RoomResponse;
import com.spaceflow.space.Space;
import com.spaceflow.space.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 사장(OWNER)의 방 관리. 모든 작업은 자기 테넌트 범위로 제한된다. */
@Service
@RequiredArgsConstructor
public class OwnerRoomService {

    private final RoomRepository roomRepository;
    private final SpaceRepository spaceRepository;

    @Transactional(readOnly = true)
    public List<RoomResponse> myRooms(Long tenantId) {
        return roomRepository.findByTenantId(tenantId).stream()
                .map(RoomResponse::from)
                .toList();
    }

    @Transactional
    public RoomResponse create(Long tenantId, RoomForm form) {
        Space space = spaceRepository.findFirstByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("등록된 지점이 없습니다."));
        Room room = roomRepository.save(
                new Room(space, form.name(), form.capacity(), form.basePricePerHour()));
        return RoomResponse.from(room);
    }

    @Transactional
    public RoomResponse update(Long tenantId, Long roomId, RoomForm form) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
        if (!room.belongsToTenant(tenantId)) {
            throw new AccessDeniedException("본인 매장의 방만 수정할 수 있습니다.");
        }
        room.update(form.name(), form.capacity(), form.basePricePerHour());
        return RoomResponse.from(room);
    }
}
