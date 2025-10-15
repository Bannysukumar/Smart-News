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

import com.bumptech.glide.Glide;
import com.way2.news.R;
import com.way2.news.models.Category;
import com.way2.news.utils.PreferenceManager;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categoryList;
    private Context context;
    private PreferenceManager preferenceManager;
    private OnCategoryClickListener onCategoryClickListener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
        void onFavoriteToggle(Category category, boolean isFavorite);
    }

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
        this.preferenceManager = new PreferenceManager(context);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.onCategoryClickListener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void updateCategoryList(List<Category> newCategoryList) {
        this.categoryList = newCategoryList;
        notifyDataSetChanged();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView categoryIcon;
        private TextView categoryName;
        private ImageView favoriteIcon;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_category);
            categoryIcon = itemView.findViewById(R.id.iv_category_icon);
            categoryName = itemView.findViewById(R.id.tv_category_name);
            favoriteIcon = itemView.findViewById(R.id.iv_favorite);

            // Set click listeners
            cardView.setOnClickListener(v -> {
                if (onCategoryClickListener != null) {
                    onCategoryClickListener.onCategoryClick(categoryList.get(getAdapterPosition()));
                }
            });

            favoriteIcon.setOnClickListener(v -> {
                if (onCategoryClickListener != null) {
                    Category category = categoryList.get(getAdapterPosition());
                    boolean isFavorite = preferenceManager.getFavoriteCategories().contains(category.getId());
                    onCategoryClickListener.onFavoriteToggle(category, isFavorite);
                }
            });
        }

        public void bind(Category category) {
            // Set category name
            categoryName.setText(category.getDisplayName());

            // Load category icon
            if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
                Glide.with(context)
                    .load(category.getIconUrl())
                    .placeholder(R.drawable.ic_category_placeholder)
                    .error(R.drawable.ic_category_placeholder)
                    .into(categoryIcon);
            } else {
                // Set default icon based on category name
                setDefaultIcon(category.getName());
            }

            // Set favorite icon state
            boolean isFavorite = preferenceManager.getFavoriteCategories().contains(category.getId());
            if (isFavorite) {
                favoriteIcon.setImageResource(R.drawable.ic_favorite_filled);
                favoriteIcon.setColorFilter(context.getResources().getColor(R.color.colorAccent));
            } else {
                favoriteIcon.setImageResource(R.drawable.ic_favorite_outline);
                favoriteIcon.setColorFilter(context.getResources().getColor(R.color.gray));
            }

            // Set category color if available
            if (category.getColor() != null && !category.getColor().isEmpty()) {
                try {
                    int color = android.graphics.Color.parseColor(category.getColor());
                    cardView.setCardBackgroundColor(color);
                } catch (Exception e) {
                    // Use default color if parsing fails
                    cardView.setCardBackgroundColor(context.getResources().getColor(R.color.white));
                }
            }
        }

        private void setDefaultIcon(String categoryName) {
            switch (categoryName.toLowerCase()) {
                case "sports":
                    categoryIcon.setImageResource(R.drawable.ic_sports);
                    break;
                case "politics":
                    categoryIcon.setImageResource(R.drawable.ic_politics);
                    break;
                case "technology":
                case "tech":
                    categoryIcon.setImageResource(R.drawable.ic_technology);
                    break;
                case "business":
                    categoryIcon.setImageResource(R.drawable.ic_business);
                    break;
                case "entertainment":
                    categoryIcon.setImageResource(R.drawable.ic_entertainment);
                    break;
                case "health":
                    categoryIcon.setImageResource(R.drawable.ic_health);
                    break;
                case "science":
                    categoryIcon.setImageResource(R.drawable.ic_science);
                    break;
                case "world":
                case "international":
                    categoryIcon.setImageResource(R.drawable.ic_world);
                    break;
                default:
                    categoryIcon.setImageResource(R.drawable.ic_category_placeholder);
                    break;
            }
        }
    }
}
