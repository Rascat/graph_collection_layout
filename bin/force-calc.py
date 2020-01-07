# FR force calculator for two vertices connected by an edge
import math


def main():
    w = int(input('width: '))
    h = int(input('height: '))
    v = tuple([int(x) for x in input('v (x y): ').split()])
    u = tuple([int(x) for x in input('u (x y): ').split()])

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
    
    # ignore min(v.disp, t) part -> might need to adapt later!
    v_new = (v[0] + v_disp[0], v[1] + v_disp[1])
    u_new = (u[0] + u_disp[0], u[1] + u_disp[1])

    out = '\n\nv: {}\nu: {}'.format(v_new, u_new)

    print(out)
    print('\nk (optimal distance): {}'.format(k))
    print('\nold distance: {}'.format(distance(v, u)))
    print('\nnew distance: {}'.format(distance(v_new, u_new)))


def distance(v, u):
    delta = (v[0] - u[0], v[1] - u[1])
    return math.sqrt(delta[0] * delta[0] + delta[1] * delta[1])


if __name__ == '__main__':
    main()
