package com.example.protectyoureyes.card.event;

import android.support.annotation.NonNull;

import com.example.protectyoureyes.card.Card;

public class DismissEvent {
    private final Card mCard;

    public DismissEvent(@NonNull final Card card) {
        mCard = card;
    }

    public Card getCard() {
        return mCard;
    }
}
