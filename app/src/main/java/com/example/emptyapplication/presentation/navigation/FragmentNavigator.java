package com.example.emptyapplication.presentation.navigation;

import androidx.annotation.AnimRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.emptyapplication.R;

/**
 * Scalable navigator for managing fragment transitions and custom animation styles
 * across the entire application.
 */
public final class FragmentNavigator {

    @IdRes
    public static final int DEFAULT_CONTAINER_ID = R.id.fragment_container;

    /**
     * Predefined transition styles. Adding a new style here makes it immediately
     * available to any screen.
     */
    public enum NavigationTransition {
        SLIDE_HORIZONTAL(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        ),
        FADE(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
        ),
        NONE(0, 0, 0, 0);

        @AnimRes public final int enter;
        @AnimRes public final int exit;
        @AnimRes public final int popEnter;
        @AnimRes public final int popExit;

        NavigationTransition(@AnimRes int enter, @AnimRes int exit, @AnimRes int popEnter, @AnimRes int popExit) {
            this.enter = enter;
            this.exit = exit;
            this.popEnter = popEnter;
            this.popExit = popExit;
        }
    }

    /**
     * Default global transition. Change this single variable to alter transitions everywhere.
     */
    public static NavigationTransition DEFAULT_TRANSITION = NavigationTransition.SLIDE_HORIZONTAL;

    private FragmentNavigator() {
        // Prevent instantiation
    }

    /**
     * Navigates to a fragment using the default container, default transition and adds to back stack.
     */
    public static void navigateTo(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        navigateTo(fragmentManager, DEFAULT_CONTAINER_ID, fragment, DEFAULT_TRANSITION, true, null);
    }

    /**
     * Navigates to a fragment with a specific transition style and adds to back stack.
     */
    public static void navigateTo(
            @NonNull FragmentManager fragmentManager,
            @NonNull Fragment fragment,
            @NonNull NavigationTransition transition
    ) {
        navigateTo(fragmentManager, DEFAULT_CONTAINER_ID, fragment, transition, true, null);
    }

    /**
     * Navigates to a fragment with full customization.
     */
    public static void navigateTo(
            @NonNull FragmentManager fragmentManager,
            @IdRes int containerId,
            @NonNull Fragment fragment,
            @NonNull NavigationTransition transition,
            boolean addToBackStack,
            String backStackTag
    ) {
        if (fragmentManager.isDestroyed()) {
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (transition != NavigationTransition.NONE) {
            transaction.setCustomAnimations(
                    transition.enter,
                    transition.exit,
                    transition.popEnter,
                    transition.popExit
            );
        }

        transaction.replace(containerId, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(backStackTag);
        }

        transaction.commit();
    }

    /**
     * Clears the entire backstack and resets to the root fragment.
     */
    public static void navigateToRoot(
            @NonNull FragmentManager fragmentManager,
            @NonNull Fragment rootFragment
    ) {
        if (fragmentManager.isDestroyed()) {
            return;
        }

        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        navigateTo(fragmentManager, DEFAULT_CONTAINER_ID, rootFragment, NavigationTransition.FADE, false, null);
    }

    /**
     * Pops the current fragment off the back stack with animation.
     */
    public static boolean popBackStack(@NonNull FragmentManager fragmentManager) {
        if (!fragmentManager.isDestroyed() && fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return true;
        }
        return false;
    }
}
