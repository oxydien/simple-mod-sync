
const HEADER = "SMS";

type LogLevel = "INFO" | "WARN" | "DEBUG";

const log = (logLevel: LogLevel, action: string, ...data: any[]) => {
    let bgColor = "";
    let textColor = "white";
    let consoleMethod = console.log;

    switch (logLevel) {
        case "INFO":
            bgColor = "#60CBFF";
            textColor = "black";
            consoleMethod = console.info;
            break;
        case "WARN":
            bgColor = "#FFD487";
            textColor = "black";
            consoleMethod = console.warn;
            break;
        case "DEBUG":
            bgColor = "#6C757D"; // Gray
            consoleMethod = console.debug;
            break;
    }

    const tagStyle = `background-color: ${bgColor}; color: ${textColor}; border-radius: 6px; font-weight: bold; font-size: 12px;`;

    const actionStyle = "color: inherit; font-weight: bold; font-size: 14px;";

    consoleMethod(
        `%c[${HEADER} - ${logLevel}]%c ${action}:`,
        tagStyle,
        actionStyle,
        ...data
    );
};

export const info = (action: string, ...data: any[]) => log("INFO", action, ...data);
export const warn = (action: string, ...data: any[]) => log("WARN", action, ...data);
export const debug = (action: string, ...data: any[]) => log("DEBUG", action, ...data);
