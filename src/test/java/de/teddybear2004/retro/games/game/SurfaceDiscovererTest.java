package de.teddybear2004.retro.games.game;

import de.teddybear2004.retro.games.minesweeper.SurfaceDiscoverer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SurfaceDiscovererTest {

    @org.junit.jupiter.api.Test
    void calculate3BV() {
        assertEquals(1, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false}
        }, new int[][]{
                {0}
        }));
        assertEquals(0, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {true}
        }, new int[][]{
                {0}
        }));

        assertEquals(1, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false, false},
                {false, false},
        }, new int[][]{
                {0, 0},
                {0, 0},
        }));
        assertEquals(3, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false, true},
                {false, false},
        }, new int[][]{
                {1, 0},
                {1, 1}
        }));
        assertEquals(2, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false, true},
                {true, false},
        }, new int[][]{
                {2, 1},
                {1, 2}
        }));


        assertEquals(1, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false, false, false},
                {false, false, false},
                {false, false, false},
        }, new int[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0},
        }));
        assertEquals(1, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false, false, true},
                {false, false, false},
                {false, false, false},
        }, new int[][]{
                {0, 1, 0},
                {0, 1, 1},
                {0, 0, 0},
        }));
        assertEquals(3, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false, false, false},
                {false, false, true},
                {false, false, false},
        }, new int[][]{
                {0, 1, 1},
                {0, 1, 0},
                {0, 1, 1},
        }));
        assertEquals(8, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false, false, false},
                {false, true, false},
                {false, false, false},
        }, new int[][]{
                {1, 1, 1},
                {1, 0, 1},
                {1, 1, 1},
        }));
        assertEquals(2, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false, false, true},
                {false, false, false},
                {false, false, true},
        }, new int[][]{
                {0, 1, 0},
                {0, 1, 1},
                {0, 1, 0},
        }));
        assertEquals(7, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false, false, false},
                {false, true, false},
                {false, false, true},
        }, new int[][]{
                {1, 1, 1},
                {1, 1, 2},
                {1, 2, 1},
        }));

        assertEquals(1, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false, false, false, false},
                {false, false, false, false},
                {false, false, false, false},
                {false, false, false, false},
        }, new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        }));
        assertEquals(4, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false, false, false, false},
                {false, false, true, false},
                {false, false, false, false},
                {false, false, false, false},
        }, new int[][]{
                {0, 1, 1, 1},
                {0, 1, 0, 1},
                {0, 1, 1, 1},
                {0, 0, 0, 0},
        }));
        assertEquals(2, SurfaceDiscoverer.calculate3BV(new boolean[][]{
                {false, false, false, true},
                {false, false, true, false},
                {false, true, false, false},
                {true, false, false, false},
        }, new int[][]{
                {0, 0, 2, 1},
                {0, 2, 2, 2},
                {2, 2, 2, 0},
                {1, 2, 0, 0},
        }));

    }

}