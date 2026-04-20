package com.eduplatform.core.media.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaAsset {

    private StoredMedia primary;

    @Builder.Default
    private List<StoredMedia> replicas = new ArrayList<>();
}
