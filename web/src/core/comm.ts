import {ResultData} from "./types/ResultData";

const apiGet = <T>(url: string | URL): Promise<ResultData<T>> => {
  const init: RequestInit = {
    method: 'GET',
    headers: new Headers({
      'Accept': 'application/json',
      'X-App': 'Simple Mod Sync generator'
    }),
    referrer: location.toString(),
  }
  return apiAny(url, init);
}

const apiAny = <T>(url: string | URL, init: RequestInit): Promise<ResultData<T>> => {
  return new Promise<ResultData<T>>(async (resolve, _) => {
    try {
      const res = await fetch(url, init);

      let text: string;
      try {
        text = await (res as Response).text();
      } catch (err) {
        resolve({
          error: `Failed to read body: ${err}`,
          error_internal: true,
        });
        return;
      }

      try {
        const json: T = JSON.parse(text as string);
        resolve({ data: json });
      } catch (err) {
        resolve({
          error: `Failed to parse body: ${err}; RAW: ${text}`,
          error_internal: true,
        });
      }
    } catch (err) {
      resolve({
        error: `Could not reach: ${err}`,
        error_internal: true,
      });
    }
  });
};


export { apiGet }