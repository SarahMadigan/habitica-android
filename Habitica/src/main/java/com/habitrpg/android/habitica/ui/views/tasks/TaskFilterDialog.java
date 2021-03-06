package com.habitrpg.android.habitica.ui.views.tasks;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.TagRepository;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TaskFilterDialog extends AlertDialog implements RadioGroup.OnCheckedChangeListener {

    @Inject
    TagRepository repository;

    @BindView(R.id.task_type_title)
    TextView taskTypeTitle;

    @BindView(R.id.task_filter_wrapper)
    RadioGroup taskFilters;
    @BindView(R.id.all_task_filter)
    RadioButton allTaskFilter;
    @BindView(R.id.second_task_filter)
    RadioButton secondTaskFilter;
    @BindView(R.id.third_task_filter)
    RadioButton thirdTaskFilter;

    @BindView(R.id.tags_title)
    TextView tagsTitleView;
    @BindView(R.id.tag_edit_button)
    Button tagsEditButton;
    @BindView(R.id.tags_list)
    LinearLayout tagsList;
    private String taskType;
    private OnFilterCompletedListener listener;

    private String filterType;
    private List<Tag> tags;
    private List<String> activeTags;

    private boolean isEditing;

    public TaskFilterDialog(Context context, AppComponent component) {
        super(context);

        component.inject(this);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_task_filter, null);

        ButterKnife.bind(this, view);

        taskFilters.setOnCheckedChangeListener(this);

        setTitle(R.string.filters);
        setView(view);
        this.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.close), (dialog, which) -> {
            if (isEditing) {
                stopEditing();
            }
            if (listener != null) {
                listener.onFilterCompleted(filterType, activeTags);
            }
            this.dismiss();
        });
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
        createTagViews();
    }

    private void createTagViews() {
        for (Tag tag : tags) {
            CheckBox tagCheckbox = new CheckBox(getContext());
            tagCheckbox.setText(tag.getName());
            tagCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!activeTags.contains(tag.getId())) {
                        activeTags.add(tag.getId());
                    }
                } else {
                    if (activeTags.contains(tag.getId())) {
                        activeTags.remove(tag.getId());
                    }
                }
            });
            tagsList.addView(tagCheckbox);
        }
        createAddTagButton();
    }

    private void createAddTagButton() {
        Button button = new Button(getContext());
        button.setText(R.string.add_tag);
        button.setOnClickListener(v -> createTag());
        tagsList.addView(button);
    }

    private void createTag() {
        Tag tag = new Tag();
        tag.id = UUID.randomUUID().toString();
        tags.add(tag);
        startEditing();
    }

    private void startEditing() {
        isEditing = true;
        tagsList.removeAllViews();
        createTagEditViews();
        tagsEditButton.setText(R.string.done);
        if (this.getWindow() != null) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    private void stopEditing() {
        isEditing = false;
        tagsList.removeAllViews();
        createTagViews();
        tagsEditButton.setText(R.string.edit_tag_btn_edit);
        if (this.getWindow() != null) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    private void createTagEditViews() {
        for (int index = 0; index < tags.size(); index++) {
            Tag tag = tags.get(index);
            createTagEditView(index, tag);
        }
        createAddTagButton();
    }

    private void createTagEditView(int index, Tag tag) {
        EditText tagEditText = new EditText(getContext());
        tagEditText.setText(tag.getName());
        tagEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tags.get(index).setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tagsList.addView(tagEditText);
    }

    public void setActiveTags(List<String> tagIds) {
        this.activeTags = tagIds;
        for (String tagId : tagIds) {
            int index = indexForId(tagId);
            if (index >= 0) {
                ((CheckBox)tagsList.getChildAt(index)).setChecked(true);
            }
        }
    }

    private int indexForId(String tagId) {
        for (int index = 0; index < tags.size(); index++) {
            if (tagId.equals(tags.get(index).id)) {
                return index;
            }
        }
        return -1;
    }

    public void setTaskType(String taskType, String activeFilter) {
        this.taskType = taskType;
        switch (taskType) {
            case Task.TYPE_HABIT: {
                taskTypeTitle.setText(R.string.habits);
                allTaskFilter.setText(R.string.all);
                secondTaskFilter.setText(R.string.weak);
                thirdTaskFilter.setText(R.string.strong);
                break;
            }
            case Task.TYPE_DAILY: {
                taskTypeTitle.setText(R.string.dailies);
                allTaskFilter.setText(R.string.all);
                secondTaskFilter.setText(R.string.active);
                thirdTaskFilter.setText(R.string.gray);
                break;
            }
            case Task.TYPE_TODO: {
                taskTypeTitle.setText(R.string.todos);
                allTaskFilter.setText(R.string.active);
                secondTaskFilter.setText(R.string.dated);
                thirdTaskFilter.setText(R.string.completed);
                break;
            }
        }
        setActiveFilter(activeFilter);
    }

    private void setActiveFilter(String activeFilter) {
        filterType = activeFilter;
        int checkedId = -1;
        if (activeFilter == null) {
            checkedId = R.id.all_task_filter;
        } else {
            switch (activeFilter) {
                case Task.FILTER_ALL:
                    checkedId = R.id.all_task_filter;
                    break;
                case Task.FILTER_WEAK:
                case Task.FILTER_DATED:
                    checkedId = R.id.second_task_filter;
                    break;
                case Task.FILTER_STRONG:
                case Task.FILTER_GRAY:
                case Task.FILTER_COMPLETED:
                    checkedId = R.id.third_task_filter;
                    break;
                case Task.FILTER_ACTIVE:
                    if (taskType.equals(Task.TYPE_DAILY)) {
                        checkedId = R.id.second_task_filter;
                    } else {
                        checkedId = R.id.all_task_filter;
                    }
                    break;
            }
        }
        taskFilters.check(checkedId);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (checkedId == R.id.all_task_filter) {
            if (!taskType.equals(Task.TYPE_TODO)) {
                filterType = Task.FILTER_ALL;
            } else {
                filterType = Task.FILTER_ACTIVE;
            }
        } else if (checkedId == R.id.second_task_filter) {
            switch (taskType) {
                case Task.TYPE_HABIT:
                    filterType = Task.FILTER_WEAK;
                    break;
                case Task.FREQUENCY_DAILY:
                    filterType = Task.FILTER_ACTIVE;
                    break;
                case Task.TYPE_TODO:
                    filterType = Task.FILTER_DATED;
                    break;
            }
        } else if (checkedId == R.id.third_task_filter) {
            switch (taskType) {
                case Task.TYPE_HABIT:
                    filterType = Task.FILTER_STRONG;
                    break;
                case Task.FREQUENCY_DAILY:
                    filterType = Task.FILTER_GRAY;
                    break;
                case Task.TYPE_TODO:
                    filterType = Task.FILTER_COMPLETED;
                    break;
            }
        }
    }

    @OnClick(R.id.tag_edit_button)
    void editButtonClicked() {
        isEditing = !isEditing;
        if (isEditing) {
            startEditing();
        } else {
            stopEditing();
        }
    }

    public void setListener(OnFilterCompletedListener listener) {
        this.listener = listener;
    }

    public interface OnFilterCompletedListener {

        void onFilterCompleted(String activeTaskFilter, List<String> activeTags);
    }
}
