/*******************************************************************************
 * Copyright 2016 stfalcon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.stfalcon.chatkit.messages;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;

import com.stfalcon.chatkit.messages.messagetypes.MessageFactory;
import com.stfalcon.chatkit.messages.typing.TypingFactory;

import io.chatcamp.sdk.BaseChannel;

/**
 * Component for displaying list of messages
 */
public class MessagesList extends RecyclerView {
    private MessagesListStyle messagesListStyle;
    private RecyclerScrollMoreListener recyclerScrollMoreListener;
    private BaseChannel channel;
    private MessagesListAdapter adapter;

    public MessagesList(Context context) {
        super(context);
    }

    public MessagesList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseStyle(context, attrs);
        init();
    }

    public MessagesList(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseStyle(context, attrs);
        init();
    }

    private void init() {
        adapter = new MessagesListAdapter(getContext());
        setAdapter(adapter);
    }

    public void addMessageFactories(MessageFactory... messageFactories) {
        adapter.addMessageFactories(messageFactories);
    }

    public void setTypingFactory(TypingFactory typingFactory) {
        adapter.addTypingFactory(typingFactory);
    }

    public void setSenderId(String senderId) {
        adapter.setSenderId(senderId);
    }

    public void setChannel(BaseChannel channel) {
        this.channel = channel;
        adapter.setChannel(channel);
    }

    /**
     * Don't use this method for setting your adapter, otherwise exception will by thrown.
     * Call {@link #setAdapter(MessagesListAdapter)} instead.
     */
    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("You can't set adapter to MessagesList. Use #setAdapter(MessagesListAdapter) instead.");
    }

    /**
     * Sets adapter for MessagesList
     *
     * @param adapter Adapter. Must extend MessagesListAdapter
     */
    public void setAdapter(MessagesListAdapter adapter) {
        setAdapter(adapter, true);
    }

    /**
     * Sets adapter for MessagesList
     *
     * @param adapter       Adapter. Must extend MessagesListAdapter
     * @param reverseLayout weather to use reverse layout for layout manager.
     */
    public void setAdapter(MessagesListAdapter adapter, boolean reverseLayout) {
        SimpleItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, reverseLayout);

        setItemAnimator(itemAnimator);
        setLayoutManager(layoutManager);
        adapter.setMessagesListStyle(messagesListStyle);
        recyclerScrollMoreListener = new RecyclerScrollMoreListener(layoutManager, adapter);
        addOnScrollListener(recyclerScrollMoreListener);
        super.setAdapter(adapter);
    }

    @SuppressWarnings("ResourceType")
    private void parseStyle(Context context, AttributeSet attrs) {
        messagesListStyle = MessagesListStyle.parse(context, attrs);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent dataFile) {
        adapter.onActivityResult(requestCode, resultCode, dataFile);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        adapter.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
