package com.wlzq.activity.redenvelope.observer;

import com.wlzq.activity.redenvelope.dto.RedEnvelopeNotifyDto;

public interface RedEnvelopeObserver {
	void notify(RedEnvelopeNotifyDto notifyDto);
}
