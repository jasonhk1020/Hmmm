package com.humdinger.hmmm;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

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

        //set the contents of the view
        if(model.getUid() != null) {
            new LoadProfileImage(imageView).execute(model.getMatchPhotoUrl());
            usernameText.setText(model.getMatchUsername());
            positionCompanyIndustryText.setText(model.getMatchInfo());
            descriptionText.setText(model.getMatchDescription());
            dialog.show();
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

    public void update(DialogModel item) {
        synchronized (mLock) {
            for (int i = 0; i < mData.size(); i++) {
                if (mData.get(i).getMatchUid().equals(item.getMatchUid())) {
                    mData.set(i, item);
                    break;
                }
            }
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

/*    public boolean exists(DialogModel item) {
        boolean found = false;
        for (DialogModel i : mData) {
            if (i.getUid() != null) {
                if (i.getUid().equals(item.getUid())) {
                    found = true;
                }
            }
        }

        return found;
    }*/

    public boolean exists(String uid) {
        boolean found = false;
        for (DialogModel i : mData) {
            if (i.getUid() != null) {
                if (i.getUid().equals(uid)) {
                    found = true;
                }
            }
        }

        return found;
    }
}
