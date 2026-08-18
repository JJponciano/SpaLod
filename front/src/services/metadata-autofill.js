import { $fetch } from "./api";

export async function extractMetadataFromXml(file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await $fetch("/api/metadata/parse/", {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    let message = `HTTP ${response.status}`;
    try {
      const data = await response.json();
      if (data.error) message = data.error;
    } catch (_) {
      // ignore JSON parse errors
    }
    throw new Error(`Failed to parse metadata XML: ${message}`);
  }

  const data = await response.json();
  return data.result || {};
}