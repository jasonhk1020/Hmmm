package com.humdinger.hmmm;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class DialogAdapter extends BaseAdapter {

    private final Context mContext;
	private final Object mLock = new Object();
	private ArrayList<DialogModel> mData;
    private View v;

	public DialogAdapter(Context context) {
		this.mContext = context;
		this.mData = new ArrayList<DialogModel>();
	}

    public DialogAdapter(Context context, View v) {
        this.mContext = context;
        this.mData = new ArrayList<DialogModel>();
        this.v = v;
    }

	public DialogAdapter(Context context, Collection<? extends DialogModel> items) {
		this.mContext = context;
		this.mData = new ArrayList<DialogModel>(items);
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		/*
        FrameLayout wrapper = new FrameLayout(mContext);
        wrapper.setBackgroundResource(R.drawable.card_bg);
        FrameLayout innerWrapper = new FrameLayout(mContext);
        innerWrapper.setBackgroundColor(mContext.getResources().getColor(R.color.card_bg));
        wrapper.addView(innerWrapper);
*/
        LayoutInflater inflater = LayoutInflater.from(getContext());
        v = inflater.inflate(R.layout.empty, parent, false);

        final DialogModel model = getDialogModel(position);

        //show dialog
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.std_dialog_inner);
        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        //setup the view
        final ImageView imageView = (ImageView) dialog.findViewById(R.id.match_dialog_image);
        final TextView usernameText = (TextView) dialog.findViewById(R.id.match_dialog_username);
        final TextView positionCompanyIndustryText = (TextView) dialog.findViewById(R.id.match_dialog_position_company_industry);
        final TextView descriptionText = (TextView) dialog.findViewById(R.id.match_dialog_description);

        if(model.getUid() != null) {
            //update views with info
            Firebase matchRef = new Firebase(getContext().getResources().getString(R.string.FIREBASE_URL)).child("users").child(model.getMatchUid());
            matchRef.addValueEventListener(new ValueEventListener() {
                // Retrieve new posts as they are added to Firebase
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    //retrieve current user info from firebase
                    Map<String, String> map = (HashMap<String, String>) snapshot.getValue();
                    String username = removeNull(map.get("username"));
                    String position = removeNull(map.get("position"));
                    String company = removeNull(map.get("company"));
                    String industry = removeNull(map.get("industry"));
                    String description = removeNull(map.get("description"));
                    String photoUrl = removeNull(map.get("photoUrl"));

                    //position company and industry into sentence
                    Spannable sPosition = new SpannableString("");
                    Spannable sCompany = new SpannableString("");
                    Spannable sIndustry = new SpannableString("");

                    //logic for adding conjunctions and bolding
                    if (!position.equals("")) {
                        sPosition = new SpannableString(position);
                        sPosition.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sPosition.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    if (!company.equals("")) {
                        if (!position.equals("")) {
                            sCompany = new SpannableString(" at " + company);
                            sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 4, sCompany.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            sCompany = new SpannableString(company);
                            sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sCompany.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                    if (!industry.equals("")) {
                        if (!position.equals("") || !company.equals("")) {
                            sIndustry = new SpannableString(" in " + industry);
                            sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 4, sIndustry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            sIndustry = new SpannableString(industry);
                            sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sIndustry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    //set the contents of the view
                    new LoadProfileImage(imageView).execute(photoUrl);
                    usernameText.setText(removeNull(username));
                    positionCompanyIndustryText.setText(TextUtils.concat(sPosition, sCompany, sIndustry));
                    descriptionText.setText(removeNull(description));
                    dialog.show();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }

        //add match toolbar
        Toolbar floatingMatchToolbar = (Toolbar) dialog.findViewById(R.id.toolbar_floating_match);
        floatingMatchToolbar.inflateMenu(R.menu.menu_floating_match);
        floatingMatchToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Map<String, Object> map;
                Firebase mFirebaseRef = new Firebase(getContext().getResources().getString(R.string.FIREBASE_URL)).child("connections").child(model.getUid());

                switch (menuItem.getItemId()) {
                    case R.id.action_floating_match_cancel:
                        map = new HashMap<String, Object>();
                        map.put(model.getMatchUid(), false);
                        mFirebaseRef.updateChildren(map, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError != null) {
                                } else {
                                    remove(model);
                                    dialog.dismiss();
                                    System.out.println("Data saved successfully.");
                                }
                            }
                        });
                        return true;
                    case R.id.action_floating_match_accept:
                        map = new HashMap<String, Object>();
                        map.put(model.getMatchUid(), true);
                        mFirebaseRef.updateChildren(map, new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError != null) {
                                } else {
                                    remove(model);
                                    dialog.dismiss();
                                    System.out.println("Data saved successfully.");
                                }
                            }
                        });
                        return true;
                    default:
                        return true;
                }
            }
        });

		return v;
	}

	public boolean shouldFillCardBackground() {
		return true;
	}

	public void add(DialogModel item) {
		synchronized (mLock) {
			mData.add(item);
		}
		notifyDataSetChanged();
	}

    public DialogModel getDialogModel(int position) {
        synchronized (mLock) {
            return mData.get(position);
        }
    }

    public void remove(DialogModel item) {
        synchronized (mLock) {
            mData.remove(item);
        }
        notifyDataSetChanged();
    }

	@Override
	public Object getItem(int position) {
		return getDialogModel(position);
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	public Context getContext() {
		return mContext;
	}

    private String removeNull(String string) {
        if (string == null) {
            string = "";
        }
        return string;
    }

    public boolean exists(DialogModel item) {
        boolean found = false;
        for (DialogModel i : mData) {
            if (i.getUid() != null) {
                if (i.getUid().equals(item.getUid())) {
                    found = true;
                }
            }
        }

        return found;
    }
}
