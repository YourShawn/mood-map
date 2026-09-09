import { act, type ReactNode } from "react";
import { createRoot, type Root } from "react-dom/client";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { FollowCenter, MapLifecycle } from "./MapLifecycle";

(globalThis as { IS_REACT_ACT_ENVIRONMENT?: boolean }).IS_REACT_ACT_ENVIRONMENT = true;

type MoveHandler = () => void;

const mapMocks = vi.hoisted(() => {
  const moveHandlers = new Set<MoveHandler>();
  const mockMap = {
    invalidateSize: vi.fn(),
    getContainer: vi.fn(),
    flyTo: vi.fn(),
    getZoom: vi.fn(() => 12),
    once: vi.fn((event: string, handler: MoveHandler) => {
      if (event === "moveend") {
        moveHandlers.add(handler);
      }
      return mockMap;
    }),
    off: vi.fn((event: string, handler: MoveHandler) => {
      if (event === "moveend") {
        moveHandlers.delete(handler);
      }
      return mockMap;
    }),
  };
  return { mockMap, moveHandlers };
});

vi.mock("react-leaflet", () => ({
  useMap: () => mapMocks.mockMap,
}));

class ResizeObserverStub {
  static instances: ResizeObserverStub[] = [];
  observe = vi.fn();
  disconnect = vi.fn();
  unobserve = vi.fn();
  constructor(public callback: ResizeObserverCallback) {
    ResizeObserverStub.instances.push(this);
  }
}

function renderNode(node: ReactNode): { root: Root; host: HTMLDivElement } {
  const host = document.createElement("div");
  document.body.appendChild(host);
  const root = createRoot(host);
  act(() => {
    root.render(node);
  });
  return { root, host };
}

describe("map size lifecycle", () => {
  let mapContainer: HTMLDivElement;

  beforeEach(() => {
    mapContainer = document.createElement("div");
    document.body.appendChild(mapContainer);
    mapMocks.mockMap.getContainer.mockReturnValue(mapContainer);
    mapMocks.mockMap.invalidateSize.mockReset();
    mapMocks.mockMap.flyTo.mockReset();
    mapMocks.mockMap.getZoom.mockReturnValue(12);
    mapMocks.mockMap.once.mockClear();
    mapMocks.mockMap.off.mockClear();
    mapMocks.moveHandlers.clear();
    ResizeObserverStub.instances = [];
    vi.stubGlobal("ResizeObserver", ResizeObserverStub);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    document.body.replaceChildren();
  });

  it("invalidates on pin count, follow token, window resize, and ResizeObserver", () => {
    const { root } = renderNode(<MapLifecycle pinCount={1} followToken={0} />);
    expect(mapMocks.mockMap.invalidateSize).toHaveBeenCalled();
    expect(ResizeObserverStub.instances).toHaveLength(1);
    expect(ResizeObserverStub.instances[0].observe).toHaveBeenCalledWith(mapContainer);

    const afterMount = mapMocks.mockMap.invalidateSize.mock.calls.length;

    act(() => {
      root.render(<MapLifecycle pinCount={2} followToken={0} />);
    });
    expect(mapMocks.mockMap.invalidateSize.mock.calls.length).toBeGreaterThan(afterMount);

    const afterPins = mapMocks.mockMap.invalidateSize.mock.calls.length;
    act(() => {
      root.render(<MapLifecycle pinCount={2} followToken={1} />);
    });
    expect(mapMocks.mockMap.invalidateSize.mock.calls.length).toBeGreaterThan(afterPins);

    const afterToken = mapMocks.mockMap.invalidateSize.mock.calls.length;
    act(() => {
      window.dispatchEvent(new Event("resize"));
    });
    expect(mapMocks.mockMap.invalidateSize.mock.calls.length).toBeGreaterThan(afterToken);

    const afterWindowResize = mapMocks.mockMap.invalidateSize.mock.calls.length;
    act(() => {
      ResizeObserverStub.instances[0].callback([] as unknown as ResizeObserverEntry[], ResizeObserverStub.instances[0]);
    });
    expect(mapMocks.mockMap.invalidateSize.mock.calls.length).toBeGreaterThan(afterWindowResize);

    act(() => {
      root.unmount();
    });
    expect(ResizeObserverStub.instances[0].disconnect).toHaveBeenCalled();
  });

  it("flies to the follow center then invalidates after flyTo and moveend", () => {
    const { root } = renderNode(
      <FollowCenter center={{ lat: 31.23, lng: 121.47 }} token={1} />
    );

    expect(mapMocks.mockMap.flyTo).toHaveBeenCalledWith([31.23, 121.47], 14, { duration: 0.7 });
    expect(mapMocks.mockMap.invalidateSize).toHaveBeenCalled();
    expect(mapMocks.moveHandlers.size).toBe(1);

    const afterFly = mapMocks.mockMap.invalidateSize.mock.calls.length;
    act(() => {
      for (const handler of [...mapMocks.moveHandlers]) {
        handler();
      }
    });
    expect(mapMocks.mockMap.invalidateSize.mock.calls.length).toBeGreaterThan(afterFly);

    act(() => {
      root.unmount();
    });
    expect(mapMocks.mockMap.off).toHaveBeenCalled();
  });
});
