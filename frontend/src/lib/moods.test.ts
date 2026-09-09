import { describe, expect, it } from "vitest";
import { MOODS, pinOpacity } from "./moods";

describe("mood catalog", () => {
  it("exposes ten one-tap moods", () => {
    expect(MOODS).toHaveLength(10);
    expect(new Set(MOODS.map((m) => m.type)).size).toBe(10);
  });

  it("fades pins toward a readable minimum", () => {
    expect(pinOpacity(1)).toBe(1);
    expect(pinOpacity(0)).toBeCloseTo(0.28);
    expect(pinOpacity(0.5)).toBeGreaterThan(0.28);
  });
});
