package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.ImageRequest;
import com.swfte.sdk.models.ImageResponse;

/**
 * Images API resource.
 */
public class Images {
    
    private final HttpClient httpClient;
    
    public Images(SwfteClient client) {
        this.httpClient = new HttpClient(client);
    }
    
    /**
     * Generate images from a text prompt.
     *
     * @param request the image generation request
     * @return image response with URLs or base64 data
     */
    public ImageResponse generate(ImageRequest request) {
        return httpClient.post("/images/generations", request, ImageResponse.class);
    }
}

