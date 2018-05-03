package com.stfalcon.chatkit.messages;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.R;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IActionContent;
import com.stfalcon.chatkit.commons.models.IActionSubContent;
import com.stfalcon.chatkit.commons.models.MessageContentType;
import com.stfalcon.chatkit.messages.customContent.Product;
import com.stfalcon.chatkit.messages.database.ChatCampDatabaseHelper;
import com.stfalcon.chatkit.preview.MediaPreviewActivity;
import com.stfalcon.chatkit.preview.WebViewActivity;
import com.stfalcon.chatkit.utils.FilePath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.GroupChannelListQuery;
import io.chatcamp.sdk.Message;
import io.chatcamp.sdk.OpenChannel;
import io.chatcamp.sdk.Participant;
import io.chatcamp.sdk.PreviousMessageListQuery;
import io.chatcamp.sdk.User;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import static android.app.Activity.RESULT_OK;
import static com.stfalcon.chatkit.messages.ConversationMessage.TYPING_TEXT_ID;

/**
 * Created by shubhamdhabhai on 16/04/18.
 */

public class ConversationViewHelper{ /*implements MessagesListAdapter.OnLoadMoreListener {
    public static final String GROUP_CONNECTION_LISTENER = "group_channel_connection";
    public static final String CHANNEL_LISTENER = "group_channel_listener";
    private static final int PICK_MEDIA_RESULT_CODE = 111;
    private static final int PICKFILE_RESULT_CODE = 120;
    private static final int CAPTURE_MEDIA_RESULT_CODE = 121;
    private static final int PREVIEW_FILE_RESULT_CODE = 112;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_MEDIA = 113;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_DOCUMENT = 114;
    private static final int PERMISSIONS_REQUEST_CAMERA = 115;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 116;
    private static final String DOCUMENT = "document";
    private static final String MEDIA = "media";
    private final String userId;
    private MessagesList messagesList;
    private MessagesListAdapter<ConversationMessage> messageMessagesListAdapter;
    //    private ImageLoader imageLoader;
    private GroupChannelListQuery.ParticipantState groupFilter;
    private String channelType;
    private String channelId;
    private MessageInput input;
    private MessageTextWatcher textWatcher;
    private GroupChannel g;
    private MaterialProgressBar progressBar;
    private TextView groupTitleTv;
    private ImageView groupImageIv;
    private Toolbar toolbar;
    private boolean isOneToOneConversation;
    private Participant otherParticipant = null;
    private MessageHolders holder;
    private String currentPhotoPath;
    private MessageContentType.Document document;
    private ChatCampDatabaseHelper databaseHelper;
    private String previousMessageId;
    private PreviousMessageListQuery previousMessageListQuery;
    private final Context context;

    private ImageLoader imageLoader;

    public interface OnGetChannelListener {
        void getChannel(BaseChannel channel);
    }

    private OnGetChannelListener onGetChannelListener;

    //TODO may be use builder pattern
    public ConversationViewHelper(Context context, MessagesList messagesList,
                                  MessageInput messageInput, MaterialProgressBar progressBar,
                                  String channelType, String channelId, String userId) {
        this.context = context;
        this.messagesList = messagesList;
        input = messageInput;
        this.progressBar = progressBar;
        this.channelType = channelType;
        this.channelId = channelId;
        this.userId = userId;
    }

    public void setOnGetChannelListener(OnGetChannelListener onGetChannelListener) {
        this.onGetChannelListener = onGetChannelListener;
    }

    public void init() {
        addConnectionListener();
        databaseHelper = new ChatCampDatabaseHelper(context);
        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.with(context).load(url)
                        .placeholder(R.drawable.icon_default_contact)
                        .error(R.drawable.icon_default_contact).into(imageView);
            }

            @Override
            public void loadImageWithPlaceholder(ImageView imageView, String url) {
                Picasso.with(context).load(url)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder).into(imageView);
            }
        };
        holder = new MessageHolders();
        holder.setOnActionItemClickedListener(new MessageHolders.OnActionItemClickedListener() {
            @Override
            public void onActionItemClicked(String url) {
                g.markAsRead();
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra(WebViewActivity.URL, url);
                context.startActivity(intent);
            }

            @Override
            public void onActionContentActionClicked(IActionContent actionContent) {
                String message = actionContent.getTitle();
                String customType = "flight-confirm-booking";
                if(actionContent.getActions().size() > 0) {
                    String action = actionContent.getActions().get(0);
                    if(action.trim().equalsIgnoreCase("make payment")) {
                        customType = "flight_make_payment";
                        message = "I want to make payment.";
                    } else if(action.trim().equalsIgnoreCase("confirm seat")) {
                        customType = "flight_confirm_seats";
                        message = "Seat confirmed - 3F";
                    }
                    else {
                        if(message != null) {
                            message = Html.fromHtml(message.split("<br>")[0]).toString() + " - ";
                        } else {
                            message = "";
                        }
                        for (IActionSubContent actionSubContent : actionContent.getContents()) {
                            for (String subContentAction : actionSubContent.getActions()) {
                                message = message  + subContentAction + ", ";
                            }
                        }
                        message = message.replaceAll(", $", "");
                    }
                }

                String meta = new Gson().toJson(actionContent);
                Product product = new Product(meta);

//                g.sendMessage(message, product, customType, new GroupChannel.SendMessageListener() {
//                    @Override
//                    public void onSent(Message message, ChatCampException e) {
//                        g.markAsRead();
//                    }
//                });

                // Toast.makeText(ConversationActivity.this, new Gson().toJson(actionContent), Toast.LENGTH_LONG).show();
                Log.d("action Content", new Gson().toJson(actionContent));
            }
        });
        holder.setOnVideoItemClickedListener(new MessageHolders.OnVideoItemClickedListener() {
            @Override
            public void onVideoItemClicked(String url) {
                g.markAsRead();
                Intent intent = new Intent(context, MediaPreviewActivity.class);
                intent.putExtra(MediaPreviewActivity.VIDEO_URI, url);
                context.startActivity(intent);
            }
        });
        holder.setOnDocumentItemClickedListener(new MessageHolders.OnDocumentItemClickedListener() {
            @Override
            public void onDocumentItemClicked(MessageContentType.Document message) {
                g.markAsRead();
                downloadAndOpenDocument(message);

            }
        });

        messageMessagesListAdapter = new MessagesListAdapter<>(userId, holder, imageLoader);
        messageMessagesListAdapter.setLoadMoreListener(this);
        messagesList.setAdapter(messageMessagesListAdapter);
        if (channelType.equals("group")) {
            messageMessagesListAdapter.addToEnd(databaseHelper.getMessages(channelId, BaseChannel.ChannelType.GROUP), false);
        } else {
            messageMessagesListAdapter.addToEnd(databaseHelper.getMessages(channelId, BaseChannel.ChannelType.OPEN), false);
        }
    }

    private void groupInit(final GroupChannel groupChannel) {
        g = groupChannel;
        g.markAsRead();
        if (g.getParticipants().size() <= 2 && g.isDistinct()) {
            isOneToOneConversation = true;
        }
        setInputListener(g);
        addTextWatcher(g);
        addChannelListener(g);

        previousMessageListQuery = g.createPreviousMessageListQuery();
        loadMessages();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addTextWatcher(final GroupChannel groupChannel) {
        textWatcher = new MessageTextWatcher(groupChannel);
        input.getInputEditText().addTextChangedListener(textWatcher);
        input.getInputEditText().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (g != null) {
                        g.markAsRead();
                    }
                }
                return false;
            }
        });
    }

    private void addChannelListener(final GroupChannel groupChannel) {
        ChatCamp.addChannelListener(CHANNEL_LISTENER, new ChatCamp.ChannelListener() {
            @Override
            public void onOpenChannelMessageReceived(OpenChannel openChannel, Message message) {
                final Message m = message;
                Toast.makeText(context, m.getText(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onGroupChannelMessageReceived(GroupChannel channel, Message message) {
                if (channel.getId().equals(groupChannel.getId())) {
                    final Message m = message;
                    final ConversationMessage conversationMessage = new ConversationMessage(m);
                    databaseHelper.addMessage(conversationMessage, channel.getId(), BaseChannel.ChannelType.GROUP);
                    messageMessagesListAdapter.addToStart(conversationMessage, true);
                    Toast.makeText(context, m.getText(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onGroupChannelTypingStatusChanged(GroupChannel channel) {
                if (channel.isTyping()) {
                    List<Participant> participants = channel.getTypingParticipants();
                    List<Participant> otherPaticipants = new ArrayList<>(participants);
                    Iterator<Participant> participantIterator = otherPaticipants.iterator();
                    while (participantIterator.hasNext()) {
                        Participant participant = participantIterator.next();
                        if (participant.getId().equals(userId)) {
                            participantIterator.remove();
                            break;
                        }
                    }
                    List<ConversationMessage> toBeRemovedMessage = new ArrayList<>();
                    List<ConversationMessage> conversationMessages = messageMessagesListAdapter.getMessageList();
                    if (conversationMessages != null
                            && conversationMessages.size() > 0) {

                        for (int i = 0; i < conversationMessages.size(); ++i) {
                            if (conversationMessages.get(i) instanceof ConversationMessage) {
                                ConversationMessage message = conversationMessages.get(i);
                                if (message.getId().contains(TYPING_TEXT_ID)) {
                                    boolean isAbsent = true;
                                    for (Participant participant : otherPaticipants) {

                                        if (message.getUser().getId().equals(participant.getId())) {
                                            isAbsent = false;
                                        }
                                    }
                                    if (isAbsent) {
                                        toBeRemovedMessage.add(message);
                                    }
                                }
                            }
                        }
                        for (Participant participant : otherPaticipants) {
                            ConversationMessage message = new ConversationMessage();
                            message.setAuthor(new ConversationAuthor(participant));
                            message.setId(TYPING_TEXT_ID + participant.getId());
                            messageMessagesListAdapter.addToStart(message, true);
                        }
                        if (toBeRemovedMessage.size() > 0) {
                            messageMessagesListAdapter.delete(toBeRemovedMessage);
                        }
                    }
                } else {
                    messageMessagesListAdapter.deleteAllTypingMessages();
                }
            }

            @Override
            public void onOpenChannelTypingStatusChanged(OpenChannel groupChannel) {

            }

            //
            @Override
            public void onGroupChannelReadStatusUpdated(GroupChannel groupChannel) {
                Map<String, Long> readReceipt = groupChannel.getReadReceipt();
                if (readReceipt.size() == groupChannel.getParticipants().size()) {
                    Long lastRead = 0L;
                    for (Map.Entry<String, Long> entry : readReceipt.entrySet()) {
                        if (lastRead == 0L || entry.getValue() < lastRead) {
                            lastRead = entry.getValue();
                        }
                    }
                    holder.setLastTimeRead(lastRead * 1000);
                    messageMessagesListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onOpenChannelReadStatusUpdated(OpenChannel groupChannel) {

            }
        });
    }

    private void addConnectionListener() {
        ChatCamp.addConnectionListener(GROUP_CONNECTION_LISTENER, new ChatCamp.ConnectionListener() {
            @Override
            public void onConnectionChanged(boolean b) {
                if (b) {
                    getChannelDetails();
                }
            }
        });
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Log.e("Conve", "Load More called");
        loadMessages();
    }

    private void downloadAndOpenDocument(final MessageContentType.Document message) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                document = message;
                if(context instanceof Activity) {
                    ((Activity)context).requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
                return;
            }
        }
        new Thread(new Runnable() {
            public void run() {
                Uri path = FileProvider.getUriForFile(context,
                        "io.chatcamp.app.fileprovider",
                        downloadFile(message.getDocumentUrl()));
//                Uri path = Uri.fromFile(downloadFile(message.getDocumentUrl()));
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    if (message instanceof ConversationMessage) {
                        intent.setDataAndType(path, ((ConversationMessage) message).getMessage().getAttachment().getType());
                    } else {
                        intent.setDataAndType(path, "application/*");
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {

                }
            }
        }).start();

    }

    public void onResume() {
        getChannelDetails();
    }

    public void onPause() {
        removeChannelListener();
        removeConnectionListener();
        removeTextWatcher();
    }

    public void onDestroy() {
        databaseHelper.close();
    }


    class MessageTextWatcher implements TextWatcher {

        private final GroupChannel groupChannel;

        public MessageTextWatcher(GroupChannel groupChannel) {
            this.groupChannel = groupChannel;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!TextUtils.isEmpty(editable)) {
                groupChannel.startTyping();
            } else {
                groupChannel.stopTyping();
            }
        }
    }

    private void getChannelDetails() {

        if (channelType.equals("open")) {
            OpenChannel.get(channelId, new OpenChannel.GetListener() {
                @Override
                public void onResult(OpenChannel openChannel, ChatCampException e) {
                    final OpenChannel o = openChannel;
                    if(onGetChannelListener != null) {
                        onGetChannelListener.getChannel(openChannel);
                    }
//                    getSupportActionBar().setTitle(o.getName());
                    openChannel.join(new OpenChannel.JoinListener() {
                        @Override
                        public void onResult(ChatCampException e) {
                            previousMessageListQuery = o.createPreviousMessageListQuery();
                            loadMessages();
                        }
                    });

                }
            });

        } else {
            //TODO check the participant state - INVITED, ALL,  ACCEPTED
            groupFilter = GroupChannelListQuery.ParticipantState.ACCEPTED;//GroupChannelListQuery.ParticipantState.valueOf(getIntent().getStringExtra("participantState"));
            GroupChannel.get(channelId, new GroupChannel.GetListener() {
                @Override
                public void onResult(final GroupChannel groupChannel, ChatCampException e) {
                    if(onGetChannelListener != null) {
                        onGetChannelListener.getChannel(groupChannel);
                    }
                    groupChannel.sync(new GroupChannel.SyncListener() {
                        @Override
                        public void onResult(ChatCampException e) {

                        }
                    });
                    if (groupFilter == GroupChannelListQuery.ParticipantState.INVITED) {
                        groupChannel.acceptInvitation(new GroupChannel.AcceptInvitationListener() {
                            @Override
                            public void onResult(GroupChannel groupChannel, ChatCampException e) {
                                groupInit(groupChannel);
                            }
                        });
                    } else {
                        groupInit(groupChannel);
                    }

                }
            });
        }
    }

    private void loadMessages() {
        Log.e("Conve", "Load Message Called");
        if (previousMessageListQuery != null) {
            previousMessageListQuery.load(20, previousMessageId, true, new PreviousMessageListQuery.ResultListener() {
                @Override
                public void onResult(List<Message> messageList, ChatCampException e) {
                    messagesList.setLoading(false);
                    final List<Message> m = messageList;
                    System.out.println("MESSSAGE HISTORY:");
                    System.out.println(m);
                    List<ConversationMessage> conversationMessages = new ArrayList<ConversationMessage>();
                    for (Message message : messageList) {
                        ConversationMessage conversationMessage = new ConversationMessage(message);
                        conversationMessages.add(conversationMessage);
                    }

                    if (TextUtils.isEmpty(previousMessageId)) {
                        databaseHelper.addMessages(conversationMessages, g.getId(), BaseChannel.ChannelType.GROUP);
                        messageMessagesListAdapter.clear();
                    }
                    Log.e("Conve", "before Message Called " + previousMessageId);

                    if (conversationMessages.size() > 0) {
                        if (TextUtils.isEmpty(previousMessageId) ||
                                !previousMessageId.equals(conversationMessages.get(conversationMessages.size() - 1)
                                        .getMessage().getId())) {
                            previousMessageId = conversationMessages.get(conversationMessages.size() - 1).getMessage().getId();
                            messageMessagesListAdapter.addToEnd(conversationMessages, false);
                            Log.e("Conve", "loading message Message Called " + previousMessageId);
                        }
                    }
                }
            });
        }
    }


    private File downloadFile(String downloadFilePath) {

        File file = null;
        try {
            File SDCardRoot = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file1 = new File(downloadFilePath);
            // create a new file, to save the downloaded file
            file = new File(SDCardRoot, file1.getName());
            if (file.exists()) {
                return file;
            }

            URL url = new URL(downloadFilePath);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();

            urlConnection.setRequestMethod("GET");
//            urlConnection.setDoOutput(true);
//
            // connect
            urlConnection.connect();

            // set the path where we want to save the file


            FileOutputStream fileOutput = new FileOutputStream(file);

            // Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            // this is the total size of the file which we are
            // downloading
            int totalsize = urlConnection.getContentLength();
            int downloadedSize = 0;

            // create a buffer...
            byte[] buffer = new byte[1024 * 1024];
            int bufferLength = 0;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                final float per = ((float) downloadedSize / totalsize) * 100;
                //TODO we should not refer activity from here
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress((int) per);
                    }
                });

            }
            //TODO we should not refer activity from here
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    progressBar.setProgress(0);
                }
            });

            // close the output stream when complete //
            fileOutput.close();

        } catch (final MalformedURLException e) {
            Log.e("document", e.getMessage());
        } catch (final IOException e) {
            Log.e("document", e.getMessage());
        } catch (final Exception e) {
            Log.e("document", e.getMessage());
        }
        return file;
    }

    private void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED ||
                    context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                //TODO try not to use activity
                ((Activity)context).requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_CAMERA);
                return;
            }
        }
        openCamera();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File file = createImageFile();
            Uri photoURI = FileProvider.getUriForFile(context,
                    "io.chatcamp.app.fileprovider",
                    file);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            Intent chooserIntent = Intent.createChooser(takePictureIntent, "Capture Image or Video");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takeVideoIntent});
            ((Activity)context).startActivityForResult(chooserIntent, CAPTURE_MEDIA_RESULT_CODE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setInputListener(final GroupChannel groupChannel) {

//        input.setAttachmentsListener(new MessageInput.AttachmentsListener() {
//            @Override
//            public void onAddAttachments() {
//                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//                // inflate the custom popup layout
//                final View inflatedView = layoutInflater.inflate(R.layout.layout_attachment, null, false);
//                LinearLayout galleryLl = inflatedView.findViewById(R.id.ll_gallery);
//                LinearLayout cameraLl = inflatedView.findViewById(R.id.ll_camera);
//                LinearLayout documentLl = inflatedView.findViewById(R.id.ll_document);
//
//                final BottomSheetDialog dialog = new BottomSheetDialog(context);
//                dialog.setContentView(inflatedView);
//                dialog.show();
//                // get device size
//                documentLl.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        dialog.hide();
//                        checkReadPermission(DOCUMENT);
//                    }
//                });
//                galleryLl.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        dialog.hide();
//                        checkReadPermission(MEDIA);
//                    }
//                });
//                cameraLl.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        dialog.hide();
//                        checkCameraPermission();
//                    }
//                });
//            }
//        });
    }

    private void checkReadPermission(String type) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (type.equalsIgnoreCase(DOCUMENT)) {
                    ((Activity)context).requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_DOCUMENT);
                } else {
                    ((Activity)context).requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_MEDIA);
                }
                return;
            }
        }
        chooseFile(type);
    }

    private void chooseFile(String type) {
        if (type.equalsIgnoreCase(DOCUMENT)) {
            Intent intent = new Intent();
            intent.setType("application/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            ((Activity)context).startActivityForResult(Intent.createChooser(intent, "Select files"), PICKFILE_RESULT_CODE);
        } else if (type.equalsIgnoreCase(MEDIA)) {
            if (Build.VERSION.SDK_INT < 19) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/* video/*");
                ((Activity)context).startActivityForResult(Intent.createChooser(intent, "Select Media"), PICK_MEDIA_RESULT_CODE);
            } else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
                ((Activity)context).startActivityForResult(intent, PICK_MEDIA_RESULT_CODE);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_DOCUMENT) {
            chooseFile(DOCUMENT);
        } else if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_MEDIA) {
            chooseFile(MEDIA);
        } else if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            downloadAndOpenDocument(document);
        } else {
            openCamera();
        }
    }

    private void removeConnectionListener() {
        ChatCamp.removeConnectionListener(GROUP_CONNECTION_LISTENER);
    }

    private void removeTextWatcher() {
        input.getInputEditText().removeTextChangedListener(textWatcher);
    }

    private void removeChannelListener() {
        ChatCamp.removeChannelListener(CHANNEL_LISTENER);
    }*/
}