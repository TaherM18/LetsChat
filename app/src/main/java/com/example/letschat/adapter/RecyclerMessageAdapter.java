package com.example.letschat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letschat.R;
import com.example.letschat.common.Common;
import com.example.letschat.databinding.ChatMessageRowBinding;
import com.example.letschat.databinding.MessageReceiverRowBinding;
import com.example.letschat.databinding.MessageSenderRowBinding;
import com.example.letschat.model.MessageModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FileUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.example.letschat.view.display.ViewImageActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kotlin.jvm.functions.Function1;

public class RecyclerMessageAdapter extends FirestoreRecyclerAdapter<MessageModel, RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater layoutInflater;
    private String chatroomId;
    private static final int VIEW_TYPE_SENDER = 1, VIEW_TYPE_RECEIVER = 2;

    private int[] reactions = new int[]{
            R.drawable.ic_fb_like,
            R.drawable.ic_fb_love,
            R.drawable.ic_fb_laugh,
            R.drawable.ic_fb_wow,
            R.drawable.ic_fb_sad,
            R.drawable.ic_fb_angry,
            R.drawable.block_24
    };


    // Store information about the long-pressed item
    private int selectedItemPosition = -1;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public RecyclerMessageAdapter(@NonNull FirestoreRecyclerOptions<MessageModel> options, Context context, String chatroomId) {
        super(options);
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.chatroomId = chatroomId;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_RECEIVER) {
            view = layoutInflater.inflate(R.layout.message_receiver_row, parent, false);
            return new ReceiverViewHolder(view);
        }
        else {
            view = layoutInflater.inflate(R.layout.message_sender_row, parent, false);
            return new SenderViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        String userId = getSnapshots().getSnapshot(position).getString("senderId");
        if (userId.equals(FirebaseUtil.currentUserId())) {
            return VIEW_TYPE_RECEIVER;
        }
        else {
            return VIEW_TYPE_SENDER;
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder itemHolder, int position, @NonNull MessageModel model) {

        // RECEIVER VIEW HOLDER ====================================================================
        // =========================================================================================

        if (itemHolder.getClass() == ReceiverViewHolder.class) {
            ReceiverViewHolder holder = (ReceiverViewHolder) itemHolder;

            ReactionsConfig config = new ReactionsConfigBuilder(context)
                    .withReactions(reactions)
                    .build();

            ReactionPopup popup = new ReactionPopup(context, config, new Function1<Integer, Boolean>() {
                @Override
                public Boolean invoke(Integer index) {
                    if (index != -1) {
                        if (index != 6) {
                            holder.binding.imgReaction.setImageResource(reactions[index]);
                            holder.binding.imgReaction.setVisibility(View.VISIBLE);
                            FirebaseUtil.getChatroomMessageReference(chatroomId).document(model.getMessageId())
                                    .update("reaction", index);
                        }
                        else {
                            holder.binding.imgReaction.setVisibility(View.GONE);
                            FirebaseUtil.getChatroomMessageReference(chatroomId).document(model.getMessageId())
                                    .update("reaction", -1);
                        }
                    }

                    return true; // true is closing popup, false is requesting a new selection
                }
            });

            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(MotionEvent e) {
                        // Perform haptic feedback
                        performHapticFeedback(context);
                        // Invoke popup.onTouch only when long press is detected
                        popup.onTouch(holder.itemView, e);
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Pass the touch event to GestureDetector to handle long press detection
                    gestureDetector.onTouchEvent(event);
                    return true; // Consume the touch event
                }
            });
            // popup setup end


            if (model.getReaction() != -1) {
                holder.binding.imgReaction.setImageResource(reactions[model.getReaction()]);
                holder.binding.imgReaction.setVisibility(View.VISIBLE);
            }
            else {
                holder.binding.imgReaction.setVisibility(View.GONE);
            }

            if (model.isRead()) {
                holder.binding.imgCheck.setImageResource(R.drawable.done_all_24);
            }
            else {
                holder.binding.imgCheck.setImageResource(R.drawable.check_24);
            }

            holder.binding.txtTime.setText(FirebaseUtil.formatTimestamp(model.getTimestamp()));
            holder.binding.txtMessage.setText(model.getMessage());

            // MESSAGE TYPE HANDLING ===============================

            try {
                switch (model.getMessageType()) {

                    case TEXT:
                        holder.binding.imgView.setVisibility(View.GONE);
                        holder.binding.imgFileType.setVisibility(View.GONE);
                        holder.binding.loadProgressBar.setVisibility(View.GONE);
                        holder.binding.txtMessage.setVisibility(View.VISIBLE);
                        holder.binding.txtTime.setVisibility(View.VISIBLE);
                        break;

                    case LOCATION:
                        // Create a clickable span for the URL
                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                // Handle URL click action here, for example, open the URL in a browser
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getMessage()));
                                intent.setPackage("com.google.android.apps.maps");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                // Customize the appearance of the clickable text if needed
                                super.updateDrawState(ds);
                                ds.setUnderlineText(true);
                                ds.setColor(context.getResources().getColor(android.R.color.holo_blue_bright)); // Change color as needed
                            }
                        };

                        // Create a SpannableString with the clickable URL
                        SpannableString spannableString = new SpannableString(model.getMessage());
                        spannableString.setSpan(clickableSpan, 0, model.getMessage().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // Set the SpannableString as the text of the TextView
                        holder.binding.txtMessage.setText(spannableString);

                        holder.binding.txtMessage.setMovementMethod(LinkMovementMethod.getInstance());

                        holder.binding.imgFileType.setImageResource(R.drawable.location_on_24);
                        holder.binding.imgView.setVisibility(View.GONE);
                        holder.binding.loadProgressBar.setVisibility(View.GONE);
                        holder.binding.imgFileType.setVisibility(View.VISIBLE);
                        holder.binding.txtMessage.setVisibility(View.VISIBLE);
                        holder.binding.txtTime.setVisibility(View.VISIBLE);
                        break;

                    case LOADING:
                        holder.binding.imgView.setVisibility(View.GONE);
                        holder.binding.imgFileType.setVisibility(View.GONE);
                        holder.binding.txtTime.setVisibility(View.GONE);
                        holder.binding.loadProgressBar.setVisibility(View.VISIBLE);
                        holder.binding.txtMessage.setVisibility(View.VISIBLE);
                        break;

                    case IMAGE:
                        Glide.with(context)
                                .load(model.getFileUrl())
                                .into(holder.binding.imgView);
                        holder.binding.txtMessage.setVisibility(View.GONE);
                        holder.binding.imgFileType.setVisibility(View.GONE);
                        holder.binding.loadProgressBar.setVisibility(View.GONE);
                        holder.binding.imgView.setVisibility(View.VISIBLE);
                        holder.binding.txtTime.setVisibility(View.VISIBLE);
                        holder.binding.imgView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                holder.binding.imgView.invalidate();
                                Drawable drawable = holder.binding.imgView.getDrawable();
                                Common.IMAGE_BITMAP = ((BitmapDrawable) drawable.getCurrent()).getBitmap();
                                ActivityOptionsCompat activityOptionsCompat =
                                        ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                                                holder.binding.imgView, "image");

                                Intent intent = new Intent(context, ViewImageActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                context.startActivity(intent, activityOptionsCompat.toBundle());
                            }
                        });
                        break;

                    case CONTACT:
                        String[] strArray = TextUtils.split(model.getMessage(), ":\n");
                        String CONTACT_NAME = strArray[0];
                        String CONTACT_NUMBER = strArray[1];
                        holder.binding.imgFileType.setImageResource(R.drawable.person_24);
                        holder.binding.imgView.setVisibility(View.GONE);
                        holder.binding.loadProgressBar.setVisibility(View.GONE);
                        holder.binding.imgFileType.setVisibility(View.VISIBLE);
                        holder.binding.txtMessage.setVisibility(View.VISIBLE);
                        holder.binding.txtTime.setVisibility(View.VISIBLE);
                        holder.binding.txtMessage.setOnClickListener(view -> openSystemSaveContactActivity(CONTACT_NAME, CONTACT_NUMBER));
                        break;

                    case DOCUMENT:
                        String[] arr = model.getMessage().split("\\.");
                        String fileExtension = arr[arr.length-1].toLowerCase();

                        switch (fileExtension) {
                            case "jpg":
                            case "jpeg":
                            case "png":
                            case "webp":
                            case "heif":
                                holder.binding.imgFileType.setImageResource(R.drawable.image_24);
                                break;
                            case "mp4":
                            case "mkv":
                            case "mov":
                                holder.binding.imgFileType.setImageResource(R.drawable.movie_24);
                                break;
                            case "gif":
                                holder.binding.imgFileType.setImageResource(R.drawable.gif_24);
                                break;
                            case "pdf":
                                holder.binding.imgFileType.setImageResource(R.drawable.pdf_24);
                                break;
                            case "html":
                                holder.binding.imgFileType.setImageResource(R.drawable.html_24);
                                break;
                            case "css":
                                holder.binding.imgFileType.setImageResource(R.drawable.css_24);
                                break;
                            case "js":
                                holder.binding.imgFileType.setImageResource(R.drawable.javascript_24);
                                break;
                            case "php":
                                holder.binding.imgFileType.setImageResource(R.drawable.php_24);
                                break;
                            default:
                                holder.binding.imgFileType.setImageResource(R.drawable.description_24);
                        }

                        holder.binding.txtMessage.setOnClickListener(v -> {
                            FileUtil.openFileInBrowser(context, model.getFileUrl());
                        });
                        holder.binding.imgView.setVisibility(View.GONE);
                        holder.binding.loadProgressBar.setVisibility(View.GONE);
                        holder.binding.txtMessage.setVisibility(View.VISIBLE);
                        holder.binding.imgFileType.setVisibility(View.VISIBLE);
                        holder.binding.txtTime.setVisibility(View.VISIBLE);
                        break;

                    case AUDIO:
                        holder.binding.imgFileType.setImageResource(R.drawable.audiotrack_24);
                        holder.binding.loadProgressBar.setVisibility(View.GONE);
                        holder.binding.imgView.setVisibility(View.GONE);
                        holder.binding.imgFileType.setVisibility(View.VISIBLE);
                        holder.binding.txtMessage.setVisibility(View.VISIBLE);
                        holder.binding.txtTime.setVisibility(View.VISIBLE);
                        holder.binding.txtMessage.setOnClickListener(v -> {
                            FileUtil.openFileInBrowser(context, model.getFileUrl());
                        });
                        break;
                }
            }
            catch (Exception e) {
                holder.binding.imgView.setVisibility(View.GONE);
                holder.binding.imgFileType.setVisibility(View.GONE);
                holder.binding.txtMessage.setVisibility(View.VISIBLE);
            }
            // MESSAGE TYPE HANDLING END ============================
        }

        // SENDER VIEW HOLDER ======================================================================
        // =========================================================================================

        else {
            SenderViewHolder holder = (SenderViewHolder) itemHolder;

            ReactionsConfig config = new ReactionsConfigBuilder(context)
                    .withReactions(reactions)
                    .build();

            ReactionPopup popup = new ReactionPopup(context, config, new Function1<Integer, Boolean>() {
                @Override
                public Boolean invoke(Integer index) {
                    if (index != -1) {
                        if (index != 6) {
                            holder.binding.imgReaction.setImageResource(reactions[index]);
                            holder.binding.imgReaction.setVisibility(View.VISIBLE);
                            FirebaseUtil.getChatroomMessageReference(chatroomId).document(model.getMessageId())
                                    .update("reaction", index);
                        }
                        else {
                            holder.binding.imgReaction.setVisibility(View.GONE);
                            FirebaseUtil.getChatroomMessageReference(chatroomId).document(model.getMessageId())
                                    .update("reaction", -1);
                        }
                    }

                    return true; // true is closing popup, false is requesting a new selection
                }
            });

            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(MotionEvent e) {
                        // Perform haptic feedback
                        performHapticFeedback(context);
                        // Invoke popup.onTouch only when long press is detected
                        popup.onTouch(holder.itemView, e);
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Pass the touch event to GestureDetector to handle long press detection
                    gestureDetector.onTouchEvent(event);
                    return true; // Consume the touch event
                }
            });
            // popup setup end


            if (model.getReaction() != -1) {
                holder.binding.imgReaction.setImageResource(reactions[model.getReaction()]);
                holder.binding.imgReaction.setVisibility(View.VISIBLE);
            }
            else {
                holder.binding.imgReaction.setVisibility(View.GONE);
            }

            holder.binding.txtTime.setText(FirebaseUtil.formatTimestamp(model.getTimestamp()));
            holder.binding.txtMessage.setText(model.getMessage());

            // MESSAGE TYPE HANDLING =============================

            try {
                switch (model.getMessageType()) {

                    case TEXT:
                        holder.binding.imgView.setVisibility(View.GONE);
                        holder.binding.imgFileType.setVisibility(View.GONE);
                        holder.binding.loadProgressBar.setVisibility(View.GONE);
                        holder.binding.txtMessage.setVisibility(View.VISIBLE);
                        holder.binding.txtTime.setVisibility(View.VISIBLE);
                        break;

                    case LOCATION:
                        // Create a clickable span for the URL
                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                // Handle URL click action here, for example, open the URL in a browser
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getMessage()));
                                intent.setPackage("com.google.android.apps.maps");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                // Customize the appearance of the clickable text if needed
                                super.updateDrawState(ds);
                                ds.setUnderlineText(true);
                                ds.setColor(context.getResources().getColor(android.R.color.holo_blue_bright)); // Change color as needed
                            }
                        };

                        // Create a SpannableString with the clickable URL
                        SpannableString spannableString = new SpannableString(model.getMessage());
                        spannableString.setSpan(clickableSpan, 0, model.getMessage().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // Set the SpannableString as the text of the TextView
                        holder.binding.txtMessage.setText(spannableString);

                        holder.binding.txtMessage.setMovementMethod(LinkMovementMethod.getInstance());

                        holder.binding.imgFileType.setImageResource(R.drawable.location_on_24);
                        holder.binding.imgView.setVisibility(View.GONE);
                        holder.binding.loadProgressBar.setVisibility(View.GONE);
                        holder.binding.imgFileType.setVisibility(View.VISIBLE);
                        holder.binding.txtMessage.setVisibility(View.VISIBLE);
                        holder.binding.txtTime.setVisibility(View.VISIBLE);
                        break;

                    case LOADING:
                        holder.binding.imgView.setVisibility(View.GONE);
                        holder.binding.imgFileType.setVisibility(View.GONE);
                        holder.binding.txtTime.setVisibility(View.GONE);
                        holder.binding.loadProgressBar.setVisibility(View.VISIBLE);
                        holder.binding.txtMessage.setVisibility(View.VISIBLE);
                        break;

                    case IMAGE:
                        Glide.with(context)
                                .load(model.getFileUrl())
                                .into(holder.binding.imgView);
                        holder.binding.txtMessage.setVisibility(View.GONE);
                        holder.binding.imgFileType.setVisibility(View.GONE);
                        holder.binding.loadProgressBar.setVisibility(View.GONE);
                        holder.binding.imgView.setVisibility(View.VISIBLE);
                        holder.binding.txtTime.setVisibility(View.VISIBLE);
                        holder.binding.imgView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AndroidUtil.transitToViewImage( context, holder.binding.imgView,
                                        model.getFileUrl() );
                            }
                        });
                        break;

                    case CONTACT:
                        String[] strArray = TextUtils.split(model.getMessage(), ":\n");
                        String CONTACT_NAME = strArray[0];
                        String CONTACT_NUMBER = strArray[1];
                        holder.binding.txtMessage.setOnClickListener(view -> openSystemSaveContactActivity(CONTACT_NAME, CONTACT_NUMBER));
                        holder.binding.imgFileType.setImageResource(R.drawable.person_24);
                        holder.binding.imgView.setVisibility(View.GONE);
                        holder.binding.loadProgressBar.setVisibility(View.GONE);
                        holder.binding.imgFileType.setVisibility(View.VISIBLE);
                        holder.binding.txtMessage.setVisibility(View.VISIBLE);
                        holder.binding.txtTime.setVisibility(View.VISIBLE);
                        break;

                    case DOCUMENT:
                        String[] arr = model.getMessage().split("\\.");
                        String fileExtension = arr[arr.length-1].toLowerCase();

                        switch (fileExtension) {
                            case "jpg":
                            case "jpeg":
                            case "png":
                            case "webp":
                            case "heif":
                                holder.binding.imgFileType.setImageResource(R.drawable.image_24);
                                break;
                            case "mp4":
                            case "mkv":
                            case "mov":
                                holder.binding.imgFileType.setImageResource(R.drawable.movie_24);
                                break;
                            case "gif":
                                holder.binding.imgFileType.setImageResource(R.drawable.gif_24);
                                break;
                            case "pdf":
                                holder.binding.imgFileType.setImageResource(R.drawable.pdf_24);
                                break;
                            case "html":
                                holder.binding.imgFileType.setImageResource(R.drawable.html_24);
                                break;
                            case "css":
                                holder.binding.imgFileType.setImageResource(R.drawable.css_24);
                                break;
                            case "js":
                                holder.binding.imgFileType.setImageResource(R.drawable.javascript_24);
                                break;
                            case "php":
                                holder.binding.imgFileType.setImageResource(R.drawable.php_24);
                                break;
                            default:
                                holder.binding.imgFileType.setImageResource(R.drawable.description_24);
                        }

                        holder.binding.txtMessage.setOnClickListener(v -> {
                            FileUtil.openFileInBrowser(context, model.getFileUrl());
                        });
                        holder.binding.imgView.setVisibility(View.GONE);
                        holder.binding.loadProgressBar.setVisibility(View.GONE);
                        holder.binding.txtMessage.setVisibility(View.VISIBLE);
                        holder.binding.imgFileType.setVisibility(View.VISIBLE);
                        holder.binding.txtTime.setVisibility(View.VISIBLE);
                        break;

                    case AUDIO:
                        holder.binding.imgFileType.setImageResource(R.drawable.audiotrack_24);
                        holder.binding.loadProgressBar.setVisibility(View.GONE);
                        holder.binding.imgView.setVisibility(View.GONE);
                        holder.binding.imgFileType.setVisibility(View.VISIBLE);
                        holder.binding.txtMessage.setVisibility(View.VISIBLE);
                        holder.binding.txtTime.setVisibility(View.VISIBLE);
                        holder.binding.txtMessage.setOnClickListener(v -> {
                            FileUtil.openFileInBrowser(context, model.getFileUrl());
                        });
                        break;
                }
            }
            catch (Exception e) {
                // holder.binding.txtMessage.setText(model.getMessage());
                holder.binding.imgView.setVisibility(View.GONE);
                holder.binding.imgFileType.setVisibility(View.GONE);
                holder.binding.txtMessage.setVisibility(View.VISIBLE);
            }
            // MESSAGE TYPE HANDLING END =============================
        }


    }

    private void openSystemSaveContactActivity(String name, String number) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType("vnd.android.cursor.dir/contact");
        intent.putExtra(android.provider.ContactsContract.Intents.Insert.NAME, name);
        intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, number);
        context.startActivity(intent);
    }

    private void performHapticFeedback(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            // Create a VibrationEffect instance
            VibrationEffect vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
            // Vibrate with the VibrationEffect
            vibrator.vibrate(vibrationEffect);
        }
    }

    // VIEW HOLDERS =================================================================================

    class ReceiverViewHolder extends RecyclerView.ViewHolder {

        MessageReceiverRowBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = MessageReceiverRowBinding.bind(itemView);
        }
    }

    class SenderViewHolder extends RecyclerView.ViewHolder {

        MessageSenderRowBinding binding;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = MessageSenderRowBinding.bind(itemView);
        }
    }
}

