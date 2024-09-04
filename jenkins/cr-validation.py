import datetime,json,urllib.*

def cr_window_check(cr_start_time,cr_end_time):
    current_time = int(datetime.datetime.now().timestamp())
    if int (cr_start_time) <= current_time <= int(cr_end_time):
        return True
    else:
        return False