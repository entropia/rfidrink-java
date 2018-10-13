package de.entropia.rfidrink;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RfidToken {
    private String uid;
}
