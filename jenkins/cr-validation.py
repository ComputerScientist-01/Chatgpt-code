import datetime,json,urllib.*

def cr_window_check(cr_start_time, cr_end_time):
    # Check if the CR window is at least 30 minutes (1800 seconds)
    if int(cr_end_time) - int(cr_start_time) < 1800:
        return False
    
    current_time = int(datetime.datetime.now().timestamp())
    
    # Check if current time is within the CR window
    if int(cr_start_time) <= current_time <= int(cr_end_time):
        return True
    else:
        return False