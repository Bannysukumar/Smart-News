package com.way2.news.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.way2.news.R;
import com.way2.news.models.Language;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {
    private List<Language> languageList;
    private Context context;
    private String selectedLanguage = "en";
    private OnLanguageClickListener onLanguageClickListener;

    public interface OnLanguageClickListener {
        void onLanguageClick(Language language);
    }

    public LanguageAdapter(Context context, List<Language> languageList) {
        this.context = context;
        this.languageList = languageList;
    }

    public void setOnLanguageClickListener(OnLanguageClickListener listener) {
        this.onLanguageClickListener = listener;
    }

    public void setSelectedLanguage(String languageCode) {
        this.selectedLanguage = languageCode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        Language language = languageList.get(position);
        holder.bind(language);
    }

    @Override
    public int getItemCount() {
        return languageList.size();
    }

    public class LanguageViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView flagImageView;
        private TextView languageNameTextView;
        private TextView englishNameTextView;
        private View selectedIndicator;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_language);
            flagImageView = itemView.findViewById(R.id.iv_flag);
            languageNameTextView = itemView.findViewById(R.id.tv_language_name);
            englishNameTextView = itemView.findViewById(R.id.tv_english_name);
            selectedIndicator = itemView.findViewById(R.id.view_selected_indicator);

            cardView.setOnClickListener(v -> {
                if (onLanguageClickListener != null) {
                    onLanguageClickListener.onLanguageClick(languageList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Language language) {
            // Set language name
            languageNameTextView.setText(language.getName());
            englishNameTextView.setText(language.getEnglishName());

            // Set flag image
            flagImageView.setImageResource(language.getFlagResource());

            // Show/hide selection indicator
            if (language.getCode().equals(selectedLanguage)) {
                selectedIndicator.setVisibility(View.VISIBLE);
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            } else {
                selectedIndicator.setVisibility(View.GONE);
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            }
        }
    }
}
