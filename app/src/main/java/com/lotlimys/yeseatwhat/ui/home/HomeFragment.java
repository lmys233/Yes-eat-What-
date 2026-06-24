package com.lotlimys.yeseatwhat.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.ui.chat.ChatActivity;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        view.findViewById(R.id.btn_chat_generate).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChatActivity.class));
        });

        view.findViewById(R.id.btn_image_generate).setOnClickListener(v -> {
            // TODO: 识图生成食谱
        });

        return view;
    }
}
