```python
from datetime import datetime

def cr_window_check(cr_start_time, cr_end_time):
    # Parse the date strings into datetime objects
    cr_start_time = datetime.strptime(cr_start_time, "%Y-%m-%d %H:%M:%S")
    cr_end_time = datetime.strptime(cr_end_time, "%Y-%m-%d %H:%M:%S")
    
    # Calculate the difference in seconds
    time_difference = (cr_end_time - cr_start_time).total_seconds()
    
    # Check if the time difference is at least 1800 seconds (30 minutes)
    if time_difference < 1800:
        return False
    
    # Get the current time
    current_time = datetime.now()
    
    # Check if the current time is within the start and end time
    if cr_start_time <= current_time <= cr_end_time:
        return True
    else:
        return False
```