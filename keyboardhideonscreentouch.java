/**
 * Function is used to identify touch event. If user touches outside of keyboard on screen instead of input control then call hide Keyboard method to hide keyboard.
 * @param view  contains parent view id of screens.
 */
@SuppressLint("ClickableViewAccessibility")
public void setupOutSideTouchHideKeyboard(final View view)
{

	// Set up touch listener for non-text box views to hide keyboard.
	if (!(view instanceof EditText))
	{

		view.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				hideKeyboard(view);
				return false;
			}

		});
	}

	// If a layout container, iterate over children and seed recursion.
	if (view instanceof ViewGroup)
	{

		for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
		{

			View innerView = ((ViewGroup) view).getChildAt(i);

			setupOutSideTouchHideKeyboard(innerView);
		}
	}
}
	
/**
 * Function is used to hide keyboard.
 * @param view  contains view, on which touch event has been performed.
 */	
public void hideKeyboard(View v)
{
	InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
	mgr.hideSoftInputFromWindow(v.getWindowToken(), 0);
}