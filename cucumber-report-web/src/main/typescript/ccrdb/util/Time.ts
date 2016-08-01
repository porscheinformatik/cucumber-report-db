class Time {
    static nsToString(ns: number): string {
        let result: string;
        if (ns != null) {
            if (ns < 1000000000) {
                if (ns / 1000000 >= 0) {
                    result = Math.round(ns / 1000000) + 'ms';
                } else {
                    result = '<1ms';
                }
            } else {
                const days = ns / 86400000000000;
                result = (days >= 1 ? (days.toFixed() + ' Day' + (days >= 2 ? 's ' : ' ')) : '')
                    + new Date(ns / 1000000).toISOString().substr(11, 8);
            }
        } else {
            result = '';
        }

        return result;
    }
}