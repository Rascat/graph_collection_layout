#!/usr/bin/env python3
# FR force calculator for two vertices connected by an edge
import argparse
import math

def main():
    parser = argparse.ArgumentParser(description='Compute displacement of two vertices connected by an edge')
    parser.add_argument('v', type=str, help='position of vertex v (x, y)')
    parser.add_argument('u', type=str, help='position of vertex u (x, y)')
    parser.add_argument('width', type=int, help='width of the layout space')
    parser.add_argument('height', type=int, help='height of the layout space')
    parser.add_argument('-t', type=float, help='cooling factor t - defaults to 1.0', default=1)

    args = parser.parse_args()

    w = args.width
    h = args.height
    t = args.t

    v = tuple([int(x) for x in args.v.split(',')])
    u = tuple([int(x) for x in args.u.split(',')])

    area = w * h
    k = math.sqrt(area / 2)

    # compute repulsive forces
    delta = (v[0] - u[0], v[1] - u[1])
    delta_len = math.sqrt((delta[0] * delta[0]) + (delta[1] * delta[1]))
    delta_norm = (delta[0] / delta_len, delta[1] / delta_len)

    repulsion = (k*k) / delta_len
    v_disp = (delta_norm[0] * repulsion, delta_norm[1] * repulsion)
    u_disp = (v_disp[0] * -1, v_disp[1] * -1)

    # compute attractive forces
    attraction = (delta_len * delta_len) / k
    attraction_vec = (delta_norm[0] * attraction, delta_norm[1] * attraction)
    v_disp = (v_disp[0] - attraction_vec[0], v_disp[1] - attraction_vec[1])
    u_disp = (u_disp[0] + attraction_vec[0], u_disp[1] + attraction_vec[1])

    # apply forces
    v_disp_len = math.sqrt((v_disp[0] * v_disp[0]) + (v_disp[1] * v_disp[1]))
    v_disp_norm = (v_disp[0] / v_disp_len, v_disp[1] / v_disp_len)
    u_disp_len = math.sqrt((u_disp[0] * u_disp[0]) + (u_disp[1] * u_disp[1]))
    u_disp_norm = (u_disp[0] / u_disp_len, u_disp[1] / u_disp_len)
    
    v_new_x = int(v[0] + v_disp_norm[0] * t)
    v_new_y = int(v[1] + v_disp_norm[1] * t)
    v_new_x = min(w, max(0, v_new_x))
    v_new_y = min(h, max(0, v_new_y))

    u_new_x = int(u[0] + u_disp_norm[0] * t)
    u_new_y = int(u[1] + u_disp_norm[1] * t)
    u_new_x = min(w, max(0, u_new_x))
    u_new_y = min(h, max(0, u_new_y))
    
    v_new = (v_new_x, v_new_y)
    u_new = (u_new_x, u_new_y)

    out = 'v: {}\nu: {}'.format(v_new, u_new)

    print(out)
    print('\nk (optimal distance): {}'.format(k))
    print('\nold distance: {}'.format(distance(v, u)))
    print('new distance: {}'.format(distance(v_new, u_new)))


def distance(v, u):
    delta = (v[0] - u[0], v[1] - u[1])
    return math.sqrt(delta[0] * delta[0] + delta[1] * delta[1])


if __name__ == '__main__':
    main()
